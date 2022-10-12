package edu.whu.hyk.exp;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
//import com.github.davidmoten.rtreemulti.Entry;
import edu.whu.hyk.encoding.Decoder;
import  edu.whu.hyk.encoding.Encoder;
import edu.whu.hyk.model.Point;
import edu.whu.hyk.model.PostingList;
import  edu.whu.hyk.util.DateUtil;
import  edu.whu.hyk.util.GeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricalRange {
    static private Logger logger = LoggerFactory.getLogger(HistoricalRange.class);
    static double[] spatial_range = new double[4];
    static HashMap<String, Object> passedParams;

    static long t_length;

    public static void generateQr(HashMap<String, Object> Params, int s_length, long temporal_length) {
        passedParams = Params;
        double[] S = (double[]) Params.get("spatialDomain");
        double lat = (S[0] + S[2]) / 2;
        double lon = (S[1] + S[3]) / 2;
        double lat1 = GeoUtil.increaseLat(lat, s_length);
        double lon1 = GeoUtil.increaseLng(lat,lon,s_length);
        spatial_range = new double[] {lat,lon,lat1,lon1};
        t_length = temporal_length;
    }

    public static void spatial_hytra(HashMap<Integer, HashSet<String>> planes){
        //decode spatial range
        long start = System.nanoTime();
//        for (int repeat = 0; repeat < 10; repeat++) {
            int resolution = (int) passedParams.get("resolution");
            int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[0],spatial_range[1]));
            int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[2],spatial_range[3]));
            //encode time range
//        String day = "2022-06-08";
            int t_s = 3600 * 9, t_e = 3600 * 13;
            double delta_t = 86400 / Math.pow(2, resolution);
            int k_s = (int)(t_s/delta_t), k_e = (int) (t_e/delta_t);

            //union
            HashSet<String> planes_i = new HashSet<>(), planes_j = new HashSet<>(), planes_k = new HashSet<>();
            for(int i = ij_s[0]; i <= ij_e[0]; i++) {
                if(planes.containsKey(i))
                planes_i.addAll(planes.get(i));
            }
            for(int j = ij_s[1] + (int) Math.pow(2,resolution); j <= ij_e[1] + (int) Math.pow(2,resolution); j++) {
                if(planes.containsKey(j))
                planes_j.addAll(planes.get(j));
            }
            for(int k = k_s + (int) Math.pow(2,resolution+1); k <= k_e + (int) Math.pow(2,resolution+1); k++) {
                if(planes.containsKey(k))
                planes_k.addAll(planes.get(k));
            }

            //intersection
            planes_i.retainAll(planes_j);
            planes_i.retainAll(planes_k);

        System.out.println("decoding time : " + (System.nanoTime() - start) / 1e9);
            HashSet<Integer> res = new HashSet<>();
            planes_i.forEach(cid -> {
                if(PostingList.mergeCT.containsKey(cid))
                    res.addAll(PostingList.mergeCT.get(cid));
            });

//        }
        long end = System.nanoTime();
        logger.info("[RQ Time] (Hytra spatial) --- " + (end-start)/1e9);
    }

    public static HashSet<Integer> spatial_hytra_noMerge(){
        long start = System.nanoTime();
        HashMap<Integer, String> zToCid = new HashMap<>();
        PostingList.CT.keySet().forEach(k ->{
            String[] dzl = k.split((String)passedParams.get("separator"));
            int z = Integer.parseInt(dzl[1]);
            zToCid.put(z, k);
        });
        HashSet<Integer> ret = new HashSet<>();
        for (int repeat = 0; repeat < 10; repeat++) {
            int resolution = (int) passedParams.get("resolution");
            int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[0],spatial_range[1]));
            int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[2],spatial_range[3]));
            //encode time range
//        String day = "2022-06-08";

            int t_s = 3600 * 9, t_e = 3600 * 13;
            double delta_t = 86400 / Math.pow(2, resolution);
            int k_s = (int)(t_s/delta_t), k_e = (int) (t_e/delta_t);

            HashSet<Integer> res = new HashSet<>();

            for (int i = ij_s[0]; i < ij_e[0]; i++) {
                for (int j = ij_s[1]; j < ij_e[1]; j++) {
                    for (int k = k_s; k < k_e; k++) {
                        int zOrder = Encoder.combine3(i,j,k,(Integer) passedParams.get("resolution"));
                        String cid = zToCid.get(zOrder);
                        if(PostingList.CT.containsKey(cid))
                        res.addAll(PostingList.CT.get(cid));
                    }
                }
            }
            if(repeat == 9)
                ret = res;

        }
        long end = System.nanoTime();
        logger.info("[RQ Time] (Hytra spatial no merge) --- " + (end-start)/1e10);

        return ret;
    }

//    public static void threeDRTree(HashMap<Integer, List<edu.whu.hyk.model.Point>> trajDataBase){
//
//
//        RTree<Integer, Point> tree = RTree.dimensions(3).create();
//        HashSet<Integer> tids = spatial_hytra_noMerge();
//        long start = System.nanoTime();
//        for (int tid : tids){
//            List<edu.whu.hyk.model.Point> points = trajDataBase.get(tid);
//            for (edu.whu.hyk.model.Point p : points){
//                tree = tree.add(0, Point.create(p.getLat(), p.getLon(),DateUtil.dateToTimeStamp(p.getDatetime())));
//            }
//        }
//        Iterable<Entry<Integer, Point>> results =
//                tree.search(Rectangle.create(spatial_range[0],spatial_range[1],spatial_range[2],spatial_range[3]));
//
//        long end = System.nanoTime();
//        logger.info("[RQ Time] (3DR-tree) --- " + (end-start)/1e10);
//
//
//    }


    public static com.github.davidmoten.rtree.RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> rtreeTraj(HashMap<Integer, List<edu.whu.hyk.model.Point>> trajDataBase) {

        final RTree<Integer, Rectangle>[] rtree = new RTree[]{RTree.create()};
        trajDataBase.forEach((tid, plist) -> {
            //获取每条轨迹的MBR
            List<edu.whu.hyk.model.Point> sortedLat = plist.stream().sorted(Comparator.comparing(edu.whu.hyk.model.Point::getLat)).collect(Collectors.toList());
            List<edu.whu.hyk.model.Point> sortedLon = plist.stream().sorted(Comparator.comparing(Point::getLon)).collect(Collectors.toList());

            double lat_min = sortedLat.get(0).getLat(),
                    lat_max = sortedLat.get(sortedLat.size()-1).getLat(),
                    lon_min = sortedLon.get(0).getLon(),
                    lon_max = sortedLon.get(sortedLon.size()-1).getLon();

            Rectangle mbr = Geometries.rectangleGeographic(lon_min,lat_min,lon_max,lat_max);
            rtree[0] = rtree[0].add(tid, mbr);
        });
        long start = System.nanoTime();
        Observable<Entry<Integer, Rectangle>> results =
                rtree[0].search(Geometries.rectangle(spatial_range[0],spatial_range[1],spatial_range[2],spatial_range[3]));
        long end = System.nanoTime();
        logger.info("[RQ Time](R-Tree) --- " + (end-start)/1e9);
        return rtree[0];
    }

    public static void temporal_hytra(HashMap<String, HashMap<Integer, HashSet<String>>> planes) {
        long start = System.currentTimeMillis();
        //固定spatial range
        double[] S = (double[]) passedParams.get("spatialDomain");
        double lat = (S[0] + S[2]) / 2;
        double lon = (S[1] + S[3]) / 2;
        double lat1 = GeoUtil.increaseLat(lat, 3000);
        double lon1 = GeoUtil.increaseLng(lat,lon,3000);
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(lat,lon));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(lat1,lon1));
        //变化temporal range
        long t_s = DateUtil.dateToTimeStamp("2022-06-01 09:00:00");
        long t_e = t_s + t_length;
        String datetime = DateUtil.timestampToDate(t_e);
        int D = Integer.parseInt(datetime.split(" ")[0].split("-")[2]);
        int resolution = (int) passedParams.get("resolution");
        double delta_t = 86400 / Math.pow(2, resolution);
        //
        if(D == 1){
            int k_s = (int)(3600 * 9/delta_t), k_e = (int) (3600 * 9 + t_length/delta_t);
            //union
            HashSet<String> planes_i = new HashSet<>(), planes_j = new HashSet<>(), planes_k = new HashSet<>();
            for(int i = ij_s[0]; i <= ij_e[0]; i++) {
                planes_i.addAll(planes.get("2022-06-01").get(i));
            }
            for(int j = ij_s[1] + (int) Math.pow(2,resolution); j <= ij_e[1] + (int) Math.pow(2,resolution); j++) {
                planes_j.addAll(planes.get("2022-06-01").get(j));
            }
            for(int k = k_s + (int) Math.pow(2,resolution+1); k <= k_e + (int) Math.pow(2,resolution+1); k++) {
                planes_k.addAll(planes.get("2022-06-01").get(k));
            }

            //intersection
            planes_i.retainAll(planes_j);
            planes_i.retainAll(planes_k);
            return;
        }

        HashSet<String> planes_I = new HashSet<>();
        for(int d = 1; d <= D; d++){
            String day;
            if(d < 10) {day = "2022-06-0" + d;}
            else {day = "2022-06-" + d;}
            HashSet<String> planes_i = new HashSet<>(), planes_j = new HashSet<>(), planes_k = new HashSet<>();
            for(int i = ij_s[0]; i <= ij_e[0]; i++) {
                planes_i.addAll(planes.get(day).get(i));
            }
            for(int j = ij_s[1] + (int) Math.pow(2,resolution); j <= ij_e[1] + (int) Math.pow(2,resolution); j++) {
                planes_j.addAll(planes.get(day).get(j));
            }
            for(int k = (int) Math.pow(2,resolution+1); k < (int) 3*Math.pow(2,resolution); k++) {
                planes_k.addAll(planes.get(day).get(k));
            }

            //intersection
            planes_i.retainAll(planes_j);
            planes_i.retainAll(planes_k);
            planes_I.addAll(planes_i);
        }
        long end = System.currentTimeMillis();
        logger.info("[RQ Time] (Hytra spatial) --- " + (end-start)/1e3);
        System.out.println(planes_I.size());
    }

}
