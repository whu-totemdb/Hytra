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
//        Params.put("city","nyc");
//        Params.put("spatialDomain", new double[]{40.502873,-74.252339,40.93372,-73.701241});
//        Params.put("resolution",6);
//        Params.put("separator", "@");
//        Params.put("epsilon", 30);
//        Params.put("dataSize",(int) 1.2e7);

        //悉尼参数
        Params.put("city","sydney");
        Params.put("spatialDomain", new double[]{-34,150.6,-33.6,151.3});
        Params.put("resolution",6);
        Params.put("separator", "@");
        Params.put("epsilon",30);
        Params.put("dataSize",(int)16e6);

        Encoder.setup(Params);
        Generator.setup(Params);

        //0：生成配置文件
//        buildTrajDB((String) Params.get("city"), "sample");
//        IndexingTime.setup(trajDataBase,Params);
//        IndexingTime.hytra();
//        System.out.println(PostingList.CP.size());
//        Generator.generateMap();
//        Generator.writeLsmConfig(String.format("/mnt/hyk/config/%s/lsm_config.config",Params.get("city")));
//        Generator.writeKV(String.format("/mnt/hyk/config/%s/put.txt",Params.get("city")));

        //0.1 生成TmC，TG,TC
//        build((String) Params.get("city"), "jun");
//        writeTC(String.format("/mnt/hyk/sim/%s/tmc_all_in_%d.txt", (String) Params.get("city"),(int) Params.get("resolution")));
//        Generator.generateMap();
//        Generator.writeTCWithCompaction("/mnt/hyk/sim/sydney/tc_all_in_4.txt");
//        writeTG(String.format("/mnt/hyk/sim/%s/tg_all_in_%d.txt", (String) Params.get("city"),
//                (int) Params.get("resolution")));


        //实验1：indexing time
        //1.1 缓存TrajDatabase（不用构建posting list）
//        buildTrajDB((String) Params.get("city"), "sample");
//        IndexingTime.setup(trajDataBase,Params);
        //1.2 执行xz2+和xzt
//        IndexingTime.xz2Plus();
//        IndexingTime.xzt();
        //1.3 执行hytra
//        IndexingTime.hytra();
        //1.4 执行R-Tree
//        IndexingTime.rtreeTraj();
//        IndexingTime.rtreePoint();

        //实验2：real-time range query
        //2.1：需要先缓存traj database（并构建GT和TlP）
//        buildTrajDB((String) Params.get("city"), "jun");
        //2.2：设置查询参数：query range
//        RealtimeRange.setup(trajDataBase,Params,3000);
        //2.3：构建xz2+索引
//        IndexingTime.setup(trajDataBase,Params);
//        HashMap<String, Integer> xz2Plus = IndexingTime.xz2Plus();
        //2.4：执行trajmesa
//        RealtimeRange.TrajMesa(xz2Plus);
        //2.5: 执行torch
//        RealtimeRange.torch(PostingList.GT,PostingList.TlP);
        //2.6: 执行hytra
//        RealtimeRange.hytra(PostingList.GT,PostingList.TlP);
        //2.7: 构建rtree
//        IndexingTime.setup(trajDataBase,Params);
//        RTree<Integer, com.github.davidmoten.rtree.geometry.Point> rtree = IndexingTime.rtreePoint();
        //2.8: 执行rtree
//        RealtimeRange.rtree(rtree);

//        //实验3：real-time kNN
//        //3.1：需要先缓存traj database（并构建integer版GT和TG）
//        buildTrajDB((String) Params.get("city"), "jun");
//        //3.2：设置查询参数：query range
//        RealtimekNN.setup(trajDataBase,Params,50);
//        //3.3: 执行hytra
//        RealtimekNN.hytra(PostingList.GT);
//        //3.4: 执行trajmesa
//        RealtimekNN.trajmesa(PostingList.GT, TG);
//        //3.5: 执行torch
//        RealtimekNN.torch(PostingList.GT, TG);
//        //3.6: 构建rtree
//        IndexingTime.setup(trajDataBase,Params);
//        RTree<Integer, com.github.davidmoten.rtree.geometry.Point> rtree = IndexingTime.rtreePoint();
//        //3.7: 执行r-tree
//        RealtimekNN.rtree(rtree);

        //实验4. historical range query
//        4.1 缓存trajDB用于R-tree查询
        buildTrajDB((String) Params.get("city"), "jun");
//        4.2 执行合并
        Generator.generateMap();
//        4.3 将合并结果更新到索引
        Generator.updateMergeCTandTC();
//        4.4 生成sweeping planes
        HashMap<Integer, HashSet<String>> planes = Generator.generatePlanes();
//        4.5 生成查询范围
        HistoricalRange.generateQr(Params, 3000, 0);
//        4.6 执行no merge情况下的查询
//        HistoricalRange.spatial_hytra_noMerge();
//        4.7 执行merge情况下的查询
        HistoricalRange.spatial_hytra(planes);
//        4.8 执行R-tree查询
//        HistoricalRange.rtreeTraj(trajDataBase);

        //实验5. similarity search
//        buildTrajDB((String) Params.get("city"), "jun");
        //随机选取tid
//        ArrayList<Integer> list = new ArrayList<>(trajDataBase.keySet());
//        int randIdx = new Random().nextInt(list.size());
//        int tid = list.get(randIdx);
//        Generator.generateMap();
//        Generator.updateMergeCTandTC();
//        int k = 30;
        //LORS
//        Simlarity.topkWithLORS(GT, TG.get(tid),k);
        //LOC with merging
//        Simlarity.LOC(tid, k, PostingList.mergeCT, PostingList.mergeTC);
        //LOC no merge
//        Simlarity.LOC(tid,k,PostingList.CT, PostingList.TC);

        //实验6. index keys
//        buildTrajDB((String) Params.get("city"), "jun");
        //1. before merging
//        System.out.println("#Keys [BEFORE]: " + PostingList.CT.size());
        //2. after merging
//        Generator.generateMap();
        //3. R-tree
//        IndexingTime.setup(trajDataBase,Params);
//        IndexingTime.rtreeTraj();

        //实验7. running time breakdown


    }

    public static void build(String city, String tableName){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(String.format("jdbc:sqlite:/Users/hayaku/Desktop/gtfs_rt_%s_1d.db",city.substring(0,3)));
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
            conn = DriverManager.getConnection(String.format("jdbc:sqlite:/Users/hayaku/Desktop/%s.db",city.substring(0,3)));
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
