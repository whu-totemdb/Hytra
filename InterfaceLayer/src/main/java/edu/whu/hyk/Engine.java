package edu.whu.hyk;

import com.github.davidmoten.rtree.RTree;
import edu.whu.hyk.encoding.Encoder;
import edu.whu.hyk.exp.*;
import edu.whu.hyk.merge.Generator;
import edu.whu.hyk.model.Point;
import edu.whu.hyk.model.PostingList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.*;

import static edu.whu.hyk.merge.Generator.planes;

public class Engine {
    static HashMap<String, Object> Params = new HashMap();

    static HashMap<Integer, List<Point>> trajDataBase = new HashMap<>();

    //用于相似度查询
    static HashMap<Integer,List<Integer>> TG = new HashMap<>();
    static HashMap<Integer, List<Integer>> GT = new HashMap<>();


    public static void main(String[] args) throws IOException {
        //纽约参数
        Params.put("city","nyc");
        Params.put("spatialDomain", new double[]{40.502873,-74.252339,40.93372,-73.701241});
        Params.put("resolution",6);
        Params.put("separator", "@");
        Params.put("epsilon", 30);
        Params.put("dataSize",(int) 1.2e7);

        //悉尼参数
//        Params.put("city","sydney");
//        Params.put("spatialDomain", new double[]{-34,150.6,-33.6,151.3});
//        Params.put("resolution",6);
//        Params.put("separator", "@");
//        Params.put("epsilon",30);
//        Params.put("dataSize",(int)16e6);

        Encoder.setup(Params);
        Generator.setup(Params);

        //real-time range query
        //1：需要先缓存traj database（并构建GT和TlP）
        buildTrajDB((String) Params.get("city"), "jun");
        //2：设置查询参数：query range
        RealtimeRange.setup(trajDataBase,Params,3000);
        //3: 执行查询
       RealtimeRange.hytra(PostingList.GT,PostingList.TlP);


    }

    public static void build(String city, String tableName){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(String.format("jdbc:sqlite:your_path_here",city.substring(0,3)));
            String sql = String.format("select * from %s limit %d", tableName,(int)Params.get("dataSize"));
            if((int)Params.get("dataSize") == -1){sql = String.format("select * from %s", tableName);}
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.isClosed()) {
                    System.out.println("no result is found!");
                }
                while (rs.next()){
                    int pid = rs.getInt("pid");
                    double lat = rs.getDouble("lat");
                    double lon = rs.getDouble("lon");
                    String datetime = rs.getString("datetime");
                    int tid = rs.getInt("tid");
                    Point p = new Point(pid, lat,lon,datetime,tid);
                    String cid = Encoder.encodeCube(p);
                    //tmc
                    if(!PostingList.TC.containsKey(tid)){
                        PostingList.TC.put(tid, new ArrayList<>());
                        PostingList.TC.get(tid).add(cid);
                    }
                    else{
                        //检查上一个点
                        int size = PostingList.TC.get(tid).size();
                        String last = PostingList.TC.get(tid).get(size - 1);
                        if(!last.equals(cid)){
                            PostingList.TC.get(tid).add(cid);
                        }
                    }
                    //tg
                   int  gid = Encoder.encodeGrid(lat, lon);
                    if(!TG.containsKey(tid)){
                        TG.put(tid, new ArrayList<>());
                        TG.get(tid).add(gid);
                    }
                    else{
                        //检查上一个点
                        int size = TG.get(tid).size();
                        int last = TG.get(tid).get(size - 1);
                        if(last != gid){
                            TG.get(tid).add(gid);
                        }
                    }
                    //ct
//                    if(!PostingList.CT.containsKey(cid)){
//                        PostingList.CT.put(cid, new HashSet<>());
//                    }
//                    PostingList.CT.get(cid).add(tid);
                }
                rs.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());

            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public static void buildTrajDB(String city, String tableName){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(String.format("jdbc:sqlite:your_path_here",city.substring(0,3)));
            int dataSize = (int)Params.get("dataSize");
            String sql = String.format("select * from %s limit %d", tableName,(int)Params.get("dataSize"));
            if(dataSize == -1){sql = String.format("select * from %s", tableName);}
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.isClosed()) {
                    System.out.println("no result is found!");
                }
                while (rs.next()){
                    int pid = rs.getInt("pid");
                    double lat = rs.getDouble("lat");
                    double lon = rs.getDouble("lon");
                    String datetime = rs.getString("datetime");
                    int tid = rs.getInt("tid");
                    Point p = new Point(pid, lat,lon,datetime,tid);
                    if(!trajDataBase.containsKey(tid)){
                        trajDataBase.put(tid, new ArrayList<Point>());
                    }
                    trajDataBase.get(tid).add(p);


                    String cid = Encoder.encodeCube(p);
                    if(cid != null){
                        //CP
                        HashSet<Integer> pidSet = PostingList.CP.getOrDefault(cid, new HashSet<>());
                        pidSet.add(pid);
                        PostingList.CP.put(cid, pidSet);
                        //CT
                        HashSet<Integer> tidSet = PostingList.CT.getOrDefault(cid, new HashSet<>());
                        tidSet.add(tid);
                        PostingList.CT.put(cid, tidSet);
                        //TC
                        List<String> cidSet = PostingList.TC.getOrDefault(tid, new ArrayList<>());
                        cidSet.add(cid);
                        PostingList.TC.put(tid, cidSet);
                    }
//
//                    int gid = Encoder.encodeGrid(lat, lon);
//                    PostingList.TlP.putIfAbsent(tid,pid);
//                    if(!PostingList.GT.containsKey(gid)){
//                        PostingList.GT.put(gid, new HashSet<Integer>());
//                    }
//                    PostingList.GT.get(gid).add(tid);
//                    if(!TG.containsKey(tid)){
//                        TG.put(tid, new ArrayList<>());
//                        TG.get(tid).add(gid);
//                    }
//                    int size = TG.get(tid).size();
//                    if(!Objects.equals(gid, TG.get(tid).get(size-1))){
//                        TG.get(tid).add(gid);
//                    }


//                    List<Integer> gidList = TG.getOrDefault(tid, new ArrayList<>());
//                    gidList.add(gid);
//                    TG.put(tid, gidList);
//
//                    List<Integer> tidList = GT.getOrDefault(gid, new ArrayList<>());
//                    tidList.add(tid);
//                    GT.put(gid, tidList);


                }
                rs.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());

            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public static void writeTC(String filePath) {
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            StringBuilder sb = new StringBuilder();
            PostingList.TC.entrySet().forEach(entry->sb.append(entry).append("\n"));
            writer.write(sb.toString());
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTG(String filePath) {
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            StringBuilder sb = new StringBuilder();
            TG.entrySet().forEach(entry->sb.append(entry).append("\n"));
            writer.write(sb.toString());
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeTCWithCompaction(String filePath) {
        File f = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(f, false);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            StringBuilder sb = new StringBuilder();
            PostingList.TC.entrySet().forEach(entry->sb.append(entry).append("\n"));
            writer.write(sb.toString());
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
