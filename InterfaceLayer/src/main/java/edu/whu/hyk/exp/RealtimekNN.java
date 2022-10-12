package edu.whu.hyk.exp;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import static com.github.davidmoten.rtree.geometry.Geometries.*;
import  edu.whu.hyk.encoding.Decoder;
import  edu.whu.hyk.encoding.Encoder;
import  edu.whu.hyk.model.Point;
import  edu.whu.hyk.util.GeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RealtimekNN {
    private static Logger logger = LoggerFactory.getLogger(RealtimekNN.class);

    private static int k;
    private static int resolution;
    private static HashMap<Integer, List<Point>> trajDataBase;

    private static HashMap<String, Object> passedParams;
    private static Point query;

    public static void setup(HashMap<Integer, List<Point>> trajdb, HashMap<String, Object> params, int K){
        passedParams = params;
        trajDataBase = trajdb;
        k = K;
        resolution = (int) params.get("resolution");
        int size = trajdb.size();
        int tid = (int)trajdb.keySet().toArray()[size/2];
        int length = trajDataBase.get(tid).size();
        query =trajDataBase.get(tid).get(length-1);
    }
    public static HashSet<Integer> IRS(double lat, double lon, int round){
        HashSet<Integer> res = new HashSet<>();
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

    public static List<Integer> hytra(HashMap<Integer, HashSet<Integer>> GT) {
        long start=  System.currentTimeMillis();
        for (int repeat = 0; repeat < 10; repeat++) {
            ArrayList<Integer> topk = new ArrayList<>();
            //创建优先队列，按照和query的距离排序
            PriorityQueue<Point> Q = new PriorityQueue(new Comparator<Point>() {
                final Point q = query;
                @Override
                public int compare(Point p1, Point p2) {
                    double d1 = GeoUtil.distance(q.getLat(),p1.getLat(),q.getLon(),p1.getLon());
                    double d2 = GeoUtil.distance(q.getLat(),p2.getLat(),q.getLon(),p2.getLon());
                    return Double.compare(d2, d1);
                }
            });

            //IRS
            int round = 0;
            while (Q.size() < k) {
                HashSet<Integer> can = IRS(query.getLat(), query.getLon(), round);
                for(int gid : can) {
                    if(GT.containsKey(gid))
                        GT.get(gid).forEach(tid -> {
                            int size = trajDataBase.get(tid).size();
                            Point p = trajDataBase.get(tid).get(size -1);
                            Q.add(p);
                        });
                }
                round++;
            }

            //返回topk
            while (topk.size() <= k) {
                topk.add(Objects.requireNonNull(Q.poll()).getTid());
            }
        }
        long end = System.currentTimeMillis();
        logger.info("[Real-time kNN Query Time] (hytra) --- " + (end-start)/1e3);
        return null;
    }

    public static List<Integer> torch(HashMap<Integer, HashSet<Integer>> GT, HashMap<Integer, List<Integer>> TG) {
        long start = System.currentTimeMillis();
        HashMap<Integer,Integer> candidatesWithUpperBound = new HashMap<>();
        HashSet<Integer> traj_q = new HashSet<>(TG.get(query.getTid()));
        TG.get(query.getTid()).forEach(gid -> {
            GT.get(gid).forEach(tid->{
                HashSet<Integer> traj = new HashSet<>(TG.get(tid));
                traj.retainAll(traj_q);
                candidatesWithUpperBound.put(tid,traj.size());
            });
        });

        // key for trajectory id, value for its upper bound            poll从大到小
        PriorityQueue<Map.Entry<Integer, Integer>> upperBoundRank = new PriorityQueue<>((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        upperBoundRank.addAll(candidatesWithUpperBound.entrySet());

        // key for trajectory id, value for its exact score            poll从小到大
        PriorityQueue<Map.Entry<Integer, Integer>> topKHeap = new PriorityQueue<>(Map.Entry.comparingByValue());
        double bestKth = -Integer.MAX_VALUE;
        // 4. 依次求出candidates和查询轨迹的真实相似度
        // 5. 如果R中的第k条候选轨迹比剩下未计算真实值的candidates的最大UB大，直接返回R
        while (!upperBoundRank.isEmpty()) {
            Map.Entry<Integer, Integer> entry = upperBoundRank.poll();

            if (topKHeap.size() >= k && bestKth > entry.getValue()) break; // early termination

//            List<String> candidateWithGrids = candidatesWithGrids.get(entry.getKey()); // 本身是有先后顺序的

            int exactValue = lors(TG.get(query.getTid()),TG.get(entry.getKey()));

            entry.setValue(exactValue);
            topKHeap.add(entry);
            if (topKHeap.size() > k) topKHeap.poll();

            bestKth = topKHeap.peek().getValue();
        }
        long end = System.currentTimeMillis();
        logger.info("[kNN Query Time] (Torch) --- " + (end-start)/1e3);
        return null;
    }

    public static int lors(List<Integer> traj1, List<Integer> traj2) {
        int size1 = traj1.size();
        int size2 = traj2.size();
        int[][] dp = new int[size1][size2];

        if(traj1.get(0).equals(traj2.get(0))) {dp[0][0] = 0;}

        for (int i = 1; i < size1; i++){
            if(traj1.get(i).equals(traj2.get(0))) {dp[i][0] = 1;}
            else {dp[i][0] = dp[i-1][0];}
        }

        for (int j = 1; j< size2; j++){
            if(traj2.get(j).equals(traj1.get(0))) {dp[0][j] = 1;}
            else {dp[0][j] = dp[0][j-1];}
        }

        for(int i =1; i < size1; i++){
            for (int j = 1; j < size2;j++){
                if(traj1.get(i).equals(traj2.get(j))){dp[i][j] = 1 + dp[i-1][j-1];}
                else {dp[i][j] = Math.max(dp[i][j-1], dp[i-1][j]);}
            }
        }

        return dp[size1-1][size2-1];

    }


    public static List<Integer> rtree(RTree<Integer, com.github.davidmoten.rtree.geometry.Point> tree) {

//        Observable<Entry<Integer, com.github.davidmoten.rtree.geometry.Point>> results = tree.search(Geometries.pointGeographic(query.getLon(), query.getLat()), k*100);
        PriorityQueue<Integer> can = new PriorityQueue<>((tid1, tid2)->{
            List<Point> pointList1 = trajDataBase.get(tid1);
            List<Point> pointList2 = trajDataBase.get(tid2);
            Point p1 = pointList1.get(pointList1.size() - 1);
            Point p2 = pointList2.get(pointList2.size() - 1);
            double distance1 = GeoUtil.distance(query.getLat(), p1.getLat(), query.getLon(), p1.getLon());
            double distance2 = GeoUtil.distance(query.getLat(), p2.getLat(), query.getLon(), p2.getLon());
            return (int) (distance2 - distance1);
        });
        long start= System.currentTimeMillis();
//        int radius = 500;
//        while (can.size() < k){
//            Observable<Entry<Integer, com.github.davidmoten.rtree.geometry.Point>> results = tree.search(Geometries.pointGeographic(query.getLon(), query.getLat()), radius);
//            results.toBlocking().forEach(entry -> can.add(entry.value()));
//            radius += 1000;
//        }

        Observable<Entry<Integer, com.github.davidmoten.rtree.geometry.Point>> results = tree.search(Geometries.pointGeographic(query.getLon(), query.getLat()), k * 100);
        results.toBlocking().forEach(entry -> can.add(entry.value()));
        long end = System.currentTimeMillis();
        logger.info("[kNN Query Time] (R-Tree) --- " + (end-start)/1e3);

        return null;
    }
//    public static List<Integer> rtree(RTree<Integer, com.github.davidmoten.rtree.geometry.Point> tree) {
//        long start= System.currentTimeMillis();
//        Observable<Entry<Integer, com.github.davidmoten.rtree.geometry.Point>> results = tree.search(Geometries.pointGeographic(query.getLon(), query.getLat()), k*100);
//        PriorityQueue<Integer> can = new PriorityQueue<>();
//        results.toBlocking().forEach(entry -> can.add(entry.value()));
//        long end = System.currentTimeMillis();
//        logger.info("[kNN Query Time] (R-Tree) --- " + (end-start)/1e3);
//
//        return null;
//    }

}
