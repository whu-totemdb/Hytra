package edu.whu.hyk.exp;

import  edu.whu.hyk.encoding.Decoder;
import  edu.whu.hyk.encoding.Encoder;
import  edu.whu.hyk.model.Point;
import  edu.whu.hyk.model.PostingList;
import  edu.whu.hyk.util.DateUtil;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class IndexingTime {

    private static final Logger logger = LoggerFactory.getLogger(IndexingTime.class);
    static HashMap<Integer,List<Point>> trajDataBase;

    static HashMap<String, Object> passedParams;

    public static void setup (HashMap<Integer,List<Point>> trajDB, HashMap<String, Object> Params) {
        passedParams = Params;
        trajDataBase = trajDB;
    }



    public static void hytra() {
        //更新CT,CP,TC,TP,GT,TlP
        System.out.println("进入hytra方法");
        long start = System.nanoTime();
        trajDataBase.forEach((tid,plist) -> {
           plist.forEach(p -> {
               int gid = Encoder.encodeGrid(p.getLat(),p.getLon());
               String cid = Encoder.encodeCube(p);

               if(!PostingList.CT.containsKey(cid)){
//                   PostingList.CP.put(cid, new HashSet<>());
                   PostingList.CT.put(cid, new HashSet<>());
               }
               PostingList.CT.get(cid).add(tid);
//               PostingList.CP.get(cid).add(p.getPid());

//               if(!PostingList.TC.containsKey(tid)){
//                   PostingList.TC.put(tid, new ArrayList<>());
//                   PostingList.TP.put(tid, new ArrayList<>());
//               }
//               PostingList.TC.get(tid).add(cid);
//               PostingList.TP.get(tid).add(p.getPid());

               PostingList.TlP.putIfAbsent(tid,p.getPid());

               if(!PostingList.GT.containsKey(gid)){
                   PostingList.GT.put(gid, new HashSet<>());
               }
               PostingList.GT.get(gid).add(p.getTid());

           });
        });

        long end = System.nanoTime();

        logger.info("[Indexing Time](Hytra) --- " + (end-start)/1e9);

    }


    public static HashMap<String, Integer> xz2Plus() {
        double[] S = (double[]) passedParams.get("spatialDomain");
        double deltaX = (S[2] - S[0])/Math.pow(2,(int) passedParams.get("resolution"));
        double deltaY = (S[3] - S[1])/Math.pow(2,(int) passedParams.get("resolution"));
        String sep = (String) passedParams.get("separator");

        HashMap<String, Integer> index = new HashMap<>();

        long start = System.currentTimeMillis();
        trajDataBase.forEach((tid, plist)->{
            Point template = plist.get(0);

            //获取每条轨迹的MBR
            List<Point> sortedLat = plist.stream().sorted(Comparator.comparing(Point::getLat)).collect(Collectors.toList());
            List<Point> sortedLon = plist.stream().sorted(Comparator.comparing(Point::getLon)).collect(Collectors.toList());

            double lat_min = sortedLat.get(0).getLat(),
                    lat_max = sortedLat.get(sortedLat.size()-1).getLat(),
                    lon_min = sortedLon.get(0).getLon(),
                    lon_max = sortedLon.get(sortedLon.size()-1).getLon();

            //获取左下角和右上角网格
            int start_gid = Encoder.encodeGrid(lat_min, lon_min);
            int end_gid = Encoder.encodeGrid(lat_max, lon_max);

            //扩张
            int round = 0;
            int i_e = Decoder.decodeZ2(end_gid)[0], j_e = Decoder.decodeZ2(end_gid)[1];
            int i_s = Decoder.decodeZ2(start_gid)[0], j_s = Decoder.decodeZ2(start_gid)[1];
            int i_r = i_s, j_r = j_s;
            while(i_r < i_e || j_r<j_e) {
                int gid = Decoder.enlargeGrid(start_gid,round++);
                i_r = Decoder.decodeZ2(gid)[0];
                j_r = Decoder.decodeZ2(gid)[1];
            }

            double[] half = new double[] {S[0] + deltaX * (i_r - i_s) / 2, S[1] + deltaY * (j_r - j_s) / 2};

            //设置poscode
            byte posCode = 1;
            for (Point point : plist) {
                if(point.getLat() > half[0] && point.getLon() < half[1]) {posCode ^= 2;} //1号区域
                if(point.getLat() < half[0] && point.getLon() > half[1]) {posCode ^= 4;} //2号区域
                if(point.getLat() > half[0] && point.getLon() > half[1]) {posCode ^= 8;} //3号区域
                if (posCode == 15) {break;}
            }


            //生成新的tid
            String new_tid = start_gid+sep+round+sep+posCode;
            index.putIfAbsent(new_tid,tid);

        });
        long end = System.currentTimeMillis();

        logger.info("[Indexing Time](TrajMesa XZ2+) --- " + (end-start)/1e3);
        return index;

    }

    public static void xzt() {
        long bin_len = 3600;
        long refTime = DateUtil.dateToTimeStamp("2022-06-01 00:00:00");
        int g = 20;

        long start = System.currentTimeMillis();
        trajDataBase.forEach((tid, plist) -> {
            List<Point> sortedTime = plist.stream().sorted(Comparator.comparing(Point::getDatetime)).collect(Collectors.toList());
            long t_s = DateUtil.dateToTimeStamp(sortedTime.get(0).getDatetime());
            long t_e = DateUtil.dateToTimeStamp(sortedTime.get(sortedTime.size()-1).getDatetime());
            long delta = t_e - t_s;

            int bin_num = (int) ((t_s - refTime)/bin_len);
            long tb_s = refTime + bin_num * bin_len,  tl_s = tb_s;
            long tb_e = tb_s + bin_len, tl_e = tb_e;
            long t_c = (tl_s + tl_e) / 2;

            //生成sequence code
            StringBuilder sequence = new StringBuilder();
            while(tl_s <= t_s && tl_e + delta >= t_e && sequence.length() < 20) {
                if(t_s < t_c) {sequence.append("0"); tl_e = t_c;}
                else {sequence.append("1"); tl_s = t_c;}
                t_c = (tl_s + tl_e) / 2;
            }

            if(tl_s > t_s || tl_e + delta < t_e){sequence.deleteCharAt(sequence.length() - 1);}


//            System.out.println(sequence);
            int newtid = bin_num + Encoder.bitToint(sequence.toString());
            });
        long end = System.currentTimeMillis();
        logger.info("[Indexing Time](TrajMesa XZT) --- " + (end-start)/1e3);

    }

    public static void dragoon() {}

    public static void torch(){}

    public static RTree<Integer,Rectangle> rtreeTraj() {
        long start = System.currentTimeMillis();
        final RTree<Integer, Rectangle>[] rtree = new RTree[]{RTree.create()};
        trajDataBase.forEach((tid, plist) -> {
            //获取每条轨迹的MBR
            List<Point> sortedLat = plist.stream().sorted(Comparator.comparing(Point::getLat)).collect(Collectors.toList());
            List<Point> sortedLon = plist.stream().sorted(Comparator.comparing(Point::getLon)).collect(Collectors.toList());

            double lat_min = sortedLat.get(0).getLat(),
                    lat_max = sortedLat.get(sortedLat.size()-1).getLat(),
                    lon_min = sortedLon.get(0).getLon(),
                    lon_max = sortedLon.get(sortedLon.size()-1).getLon();

            Rectangle mbr = Geometries.rectangleGeographic(lon_min,lat_min,lon_max,lat_max);
            rtree[0] = rtree[0].add(tid, mbr);
        });
        long end = System.currentTimeMillis();
        logger.info("[Indexing Time](R-Tree traj) --- " + (end-start)/1e3);
        System.out.println("#Keys [R-tree]: " + rtree[0].size());
        return rtree[0];
    }

    public static RTree<Integer, com.github.davidmoten.rtree.geometry.Point> rtreePoint(){
        long start = System.currentTimeMillis();
        RTree<Integer, com.github.davidmoten.rtree.geometry.Point> rtree = RTree.create();
//        int batch = 0;
        for(List<Point> plist : trajDataBase.values()){
            for(Point p : plist){
                com.github.davidmoten.rtree.geometry.Point point = Geometries.pointGeographic(p.getLon(),p.getLat());
                rtree = rtree.add(p.getTid(), point);
//                batch++;
//
//                if(batch % (int) 1e6 == 0){
//                    System.out.println(batch+","+(System.currentTimeMillis() - start)/1e3);
//                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("[Indexing Time](R-Tree point) --- " + (end-start)/1e3);
        return rtree;
    }



    public static double[] getSpatialDomain(String city, String tableName) {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(String.format("jdbc:sqlite:/mnt/hyk/data/bus/%s/ready/gtfs_rt_%s.db", city, city.substring(0,3)));
            String sql = String.format("select min(lat) as x_s,min(lon) as y_s,max(lat) as x_e,max(lon) as y_e from %s;", tableName);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.isClosed()) {
                    System.out.println("no result is found!");
                }
                while (rs.next()){
                    return new double[]{
                            rs.getDouble("x_s"),
                            rs.getDouble("y_s"),
                            rs.getDouble("x_e"),
                            rs.getDouble("y_e")
                    };
                }
                rs.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());

            }


        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return null;
    }
}
