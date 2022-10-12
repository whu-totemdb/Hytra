package edu.whu.hyk.exp;

import  edu.whu.hyk.encoding.Decoder;
import  edu.whu.hyk.encoding.Encoder;
import  edu.whu.hyk.model.Point;
import  edu.whu.hyk.util.GeoUtil;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RealtimeRange {

    private static Logger logger = LoggerFactory.getLogger(RealtimeRange.class);

    static double[] query_range = new double[4];

    static HashMap<Integer, List<Point>> trajDataBase;
    static HashMap<String, Object> passedParams;

    public static void setup(HashMap<Integer, List<Point>> trajDB, HashMap<String, Object> Params, int length) {
        trajDataBase = trajDB;
        passedParams = Params;
        double[] S = (double[]) Params.get("spatialDomain");
        double lat = (S[0] + S[2]) / 2;
        double lon = (S[1] + S[3]) / 2;
        double lat1 = GeoUtil.increaseLat(lat, length);
        double lon1 = GeoUtil.increaseLng(lat,lon,length);
        query_range = new double[] {lat,lon,lat1,lon1};
    }

    public static  HashSet<Integer> hytra(HashMap<Integer, HashSet<Integer>> GT, HashMap<Integer,Integer> TlP){
        long start = System.currentTimeMillis();
        HashSet<Integer> can = new HashSet<>();
        int resolution = (int) passedParams.get("resolution");
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(query_range[0],query_range[1]));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(query_range[2],query_range[3]));

        //definitive window
        HashSet<Integer> def_window = new HashSet<>();
        for(int i = ij_s[0] + 1; i < ij_e[0];i++){
            for(int j = ij_s[1] + 1; j < ij_e[1];j++){
                def_window.add(Encoder.combine2(i,j,2 * resolution));
            }
        }
        def_window.retainAll(GT.keySet());
        for (Integer gid : def_window) {
            can.addAll(GT.get(gid));
        }


        //idefinitive window
        HashSet<Integer> indef_window = new HashSet<>();
        //固定j
        for(int i = ij_s[0]; i <= ij_e[0];i++){
            indef_window.add(Encoder.combine2(i,ij_s[1], resolution * 2));
            indef_window.add(Encoder.combine2(i,ij_e[1], resolution * 2));
        }
        //固定i
        for(int j = ij_s[1]+1; j <= ij_e[1]-1;j++){
            indef_window.add(Encoder.combine2(ij_s[0],j, resolution * 2));
            indef_window.add(Encoder.combine2(ij_e[0],j, resolution * 2));
        }
        indef_window.retainAll(GT.keySet());

        for (Integer gid : indef_window) {
            GT.get(gid).forEach(tid -> {
                int size = trajDataBase.get(tid).size();
                Point p = trajDataBase.get(tid).get(size - 1);
//                if(p.getPid() == TlP.get(tid)) {
                    if(contains(query_range, p.getLat(), p.getLon()))
                        can.add(tid);
//                }
            });
        }
        long end = System.currentTimeMillis();
        logger.info("[Real-time Range Query Time](Hytra) --- " + (end - start)/1e3);
        System.out.println(can.size());
        return  can;
    }


    public static boolean contains(double[] Qr, double lat, double lon){
        return Qr[0] <= lat && lat <= Qr[2] && Qr[1] <= lon && lon <= Qr[3];
    }

    public static HashSet<Integer> torch(HashMap<Integer, HashSet<Integer>> GT, HashMap<Integer,Integer> TlP) {

        long start = System.currentTimeMillis();
        HashSet<Integer> can = new HashSet<>();

        //查询范围中心点
        double[] S = query_range;
        double lat = (S[0] + S[2]) / 2, lon = (S[1] + S[3]) / 2;

        //最大round
        int[] isjs = Decoder.decodeZ2(Encoder.encodeGrid(S[0],S[1]));
        int[] ieje = Decoder.decodeZ2(Encoder.encodeGrid(S[2],S[3]));
        int max_round = Math.max((ieje[0] - isjs[0]) /2,(ieje[1] - isjs[1]) /2 );


        //IRS查询
        int round = 0;
        while (round < max_round) {
            for (int gid : IRS(lat,lon,round)){
                if(GT.containsKey(gid))
                    can.addAll(GT.get(gid));
            }
            round++;
        }

        //refine
        for (Integer gid : IRS(lat,lon,max_round)) {
            if(GT.containsKey(gid)){
                GT.get(gid).forEach(tid -> {
                    int size = trajDataBase.get(tid).size();
                    Point p = trajDataBase.get(tid).get(size - 1);
                   if(TlP.get(tid) == p.getPid())
                       if(contains(query_range, p.getLat(), p.getLon()))
                           can.add(tid);

                });
            }
        }

        long end = System.currentTimeMillis();

        logger.info("[Real-time Range Query Time](Torch) --- " + (end - start)/1e3);
        System.out.println(max_round);
        System.out.println(can.size());

        return can;
    }

    public static void rtree(RTree<Integer, com.github.davidmoten.rtree.geometry.Point> tree) {
        HashSet<Integer> res = new HashSet<>();
        long start = System.currentTimeMillis();
        Observable<Entry<Integer, com.github.davidmoten.rtree.geometry.Point>> results =
                tree.search(Geometries.rectangleGeographic(query_range[1],query_range[0],query_range[3],query_range[2]));

           results.toBlocking().forEach(entry -> {
            if(entry.geometry().intersects(Geometries.rectangleGeographic(query_range[1],query_range[0],query_range[3],query_range[2])))
                res.add(entry.value());
           });
        long end = System.currentTimeMillis();
        logger.info("[Real-time Range Query Time](R-Tree) --- " + (end-start)/1e3);
    }

    /**
     * Incremental Range Search
     * @param lat
     * @param lon
     * @param round
     * @return gid
     */
    public static HashSet<Integer> IRS(double lat, double lon, int round){
        HashSet<Integer> res = new HashSet<>();
        int resolution = (int) passedParams.get("resolution");
        int center_gid = Encoder.encodeGrid(lat,lon);
        int[] icjc = Decoder.decodeZ2(center_gid);
        int i_s = Math.max(0, icjc[0] - round);
        int i_e = Math.max((int)Math.pow(2,resolution)-1, icjc[0] + round);
        int j_s = Math.min(0, icjc[1] - round);
        int j_e = Math.min((int)Math.pow(2,resolution)-1, icjc[1] + round);
        for (int i = i_s; i <= i_e; i++){
            res.add(Encoder.combine2(i, j_s, resolution * 2));
            res.add(Encoder.combine2(i, j_e, resolution * 2));
        }
        for (int j = j_s + 1; j < j_e; j++){
            res.add(Encoder.combine2(i_e, j, resolution * 2));
            res.add(Encoder.combine2(i_s, j, resolution * 2));
        }
        return res;
    }


}
