package org.example.wty.similarity;

import cn.edu.whu.index.*;
import cn.edu.whu.util.*;
import cn.edu.whu.socket.*;

import java.util.*;

public class ExternalStorageExperiment {
    public static List<String> R = new ArrayList<>(); // topk结果集
    static int theta = 1000;
    static int lambda = 100;

    // topk遍历初始轮数
    static final int INITIAL_ROUND_FOR_H_OR_F = 1;

    //创建octree和quadtree
    static int D = 6;
    static int MAX_LEVEL = 4; //只要修改这个参数就可以
    static String DAY = "2022-01-0"+D;
    static int MIN_CUBE_THRES = (int) Math.pow(8, 7-MAX_LEVEL);
    static double MINX = 562942;
    static double MINY = 4483842;
    static double MINZ = DateUtil.dateToTimeStamp(DAY+" 00:00:00");//2022-01-06 00:00:00
    static double MAXX = 610046;
    static double MAXY = 4532212;
    static double MAXZ = MINZ + 86400;
    static BoundingBox3D cube = new BoundingBox3D(new Point3D(MINX,MINY,MINZ), new Point3D(MAXX,MAXY,MAXZ));
    static OcTree octree = new OcTree(cube, MAX_LEVEL,DAY,MIN_CUBE_THRES);
    static BoundingBox2D grid = new BoundingBox2D(new Point2D(MINX,MINY), new Point2D(MAXX,MAXY));
    static QuadTree quadtree = new QuadTree(grid, MAX_LEVEL, DAY);

    //⾸先调⽤FileUtil.readAdjMT来构建邻接表，同样可以把4替换成5或6
    static HashMap<String, HashSet<String>> adjList = new HashMap<>();
//        //调⽤quadtree.expandByAdjList⽅法进⾏expand，需要传⼊邻接表
//        //这个⽅法返回的是[0,round]轮所有的结果，⽐如这⾥返回的是第0，1，2，3轮结果的并集。
//        HashSet<String> res = quadtree.expandByAdjList(adjList, "2022-01-06@157@0",3);

    // 三个倒排表
    static HashMap<String, ArrayList<String>> TCList = new HashMap<String, ArrayList<String>>(); // trajectoryID-cubeIDs
    static HashMap<String, ArrayList<String>> CTList = new HashMap<String, ArrayList<String>>(); // cubeID-trajectoryIDs
    static HashMap<String, ArrayList<String>> GTList = new HashMap<String, ArrayList<String>>(); // gridID-trajectoryIDs
    static HashMap<String, ArrayList<String>> TGList = new HashMap<String, ArrayList<String>>(); // trajectoryID-gridIDs

    public static void main (String[] args) {


        //创建socket client
        SocketClient client = new SocketClient();
        client.put("/home/hyk/config/6/put.txt");
        //调⽤client的getTCList⽅法，这⾥路径中的4代表maxLevel，可以替换成5，6

        // 从外存取出TCList用时
        long startTimeCTList = System.currentTimeMillis(); //获取开始时间
//        TCList =  client.getTCList("/home/hyk/config/6/cid/0106.txt");
        TGList = client.getTGList(octree,quadtree,"/home/hyk/config/6/cid/0106.txt");
        long endTimeCTList = System.currentTimeMillis(); //获取结束时间
        System.out.println("外存取出TGList查询时间：" + (endTimeCTList - startTimeCTList) + "ms");

//        HashMap<String, HashSet<String>> adjList_temp = new HashMap<>();
        adjList = FileUtil.readAdjMT("/home/hyk/config/6/adjlist/0106.txt");
//        Set<String> adjkeys = adjList_temp.keySet();
//        for(String adjkey : adjkeys) {
//            HashSet<String> adjcubes = adjList_temp.get(adjkey);
//            HashSet<String> adjgrids = new HashSet<>();
//            for(String adjcube : adjcubes) {
//                String adjgrid = octree.toGid(adjcube, quadtree);
//                adjgrids.add(adjgrid);
//            }
//            adjList.put(adjkey, adjgrids);
//        }


//        CTList = new HashMap<String, ArrayList<String>>(); // cubeID-trajectoryIDs
        GTList = new HashMap<String, ArrayList<String>>(); // gridID-trajectoryIDs

        Set<String> keys = TGList.keySet();
        for(String key : keys) {
            ArrayList<String> gridIDs = TGList.get(key);
            for(String gridID : gridIDs) {
                ArrayList<String> traIDs = new ArrayList<>();
                if(GTList.containsKey(gridID)) {
                    traIDs = GTList.get(gridID);
                    traIDs.add(key);
                    GTList.put(gridID,traIDs);
                } else {
                    traIDs.add(key);
                    GTList.put(gridID, traIDs);
                }
            }
        }

//        Set<String> keys = TCList.keySet();
//        for(String key : keys) {
//            ArrayList<String> cubeIDs = TCList.get(key);
//            for(String cubeID : cubeIDs) {
//                ArrayList<String> traIDs = new ArrayList<>();
//                if(CTList.containsKey(cubeID)) {
//                    traIDs = CTList.get(cubeID);
//                    traIDs.add(key);
//                    CTList.put(cubeID, traIDs);
//                } else {
//                    traIDs.add(key);
//                    CTList.put(cubeID, traIDs);
//                }
//
//                ArrayList<String> gridtraIDs = new ArrayList<>();
//                String gridID = octree.toGid(cubeID, quadtree);
//                if(GTList.containsKey(gridID)) {
//                    gridtraIDs = GTList.get(gridID);
//                    if(!gridtraIDs.contains(key)) {
//                        gridtraIDs.add(key);
//                        GTList.put(gridID,gridtraIDs);
//                    }
//                } else {
//                    gridtraIDs.add(key);
//                    GTList.put(gridID,gridtraIDs);
//                }
//            }
//        }

        System.out.println(TGList);

        // 创建输入对象
        Scanner sc = new Scanner(System.in);

        // 获取用户输入的字符串
        String queryTraID = null;
        System.out.print("请输入轨迹id：");
        queryTraID = sc.nextLine();

        System.out.println("输入的轨迹id：" + queryTraID);

//        // 存储轨迹id经过的cubes
//        List<String> queryTraCubeIDs = new ArrayList<>();
//        queryTraCubeIDs = TCList.get(queryTraID);
////        System.out.println(queryTraCubeIDs);
//
//        // 存储轨迹id经过的grids
//        List<String> queryTraGridIDs = new ArrayList<>();
//        for (int i = 0; i < queryTraCubeIDs.size(); i++){
//            String gid = octree.toGid(queryTraCubeIDs.get(i), quadtree);
//            if (i > 0 && queryTraGridIDs.get(queryTraGridIDs.size() - 1).equals(gid)) continue;
//            queryTraGridIDs.add(gid);
//        }

        List<String> queryTraGridIDs = TGList.get(queryTraID);

        // topk的k
        int k = 20;
        // 进行topk查询
        long startTime1 = System.currentTimeMillis(); //获取开始时间
        topkWithLORS(queryTraID, queryTraGridIDs, k, R);
        long endTime1 = System.currentTimeMillis(); //获取结束时间
        System.out.println("LORS-Top-k查询时间：" + (endTime1 - startTime1) + "ms");

        // 进行topk查询
        long startTime2 = System.currentTimeMillis(); //获取开始时间
        topkWithLCSS(queryTraID, queryTraGridIDs, k, R);
        long endTime2 = System.currentTimeMillis(); //获取结束时间
        System.out.println("LCSS-Top-k查询时间：" + (endTime2 - startTime2) + "ms");

        // 进行topk查询
        long startTime3 = System.currentTimeMillis(); //获取开始时间
        topkWithFrechetOrHausdorff(queryTraID, queryTraGridIDs, k, R, "h");
        long endTime3 = System.currentTimeMillis(); //获取结束时间
        System.out.println("Hausdorff-Top-k查询时间：" + (endTime3 - startTime3) + "ms");

        // 进行topk查询
        long startTime4 = System.currentTimeMillis(); //获取开始时间
        topkWithFrechetOrHausdorff(queryTraID, queryTraGridIDs, k, R, "f");
        long endTime4 = System.currentTimeMillis(); //获取结束时间
        System.out.println("Frechet-Top-k查询时间：" + (endTime4 - startTime4) + "ms");

    }

    //==========================================================LORS=========================================================================================================
    // 计算两条轨迹相似度：把cubes转化成对应的grids然后用LORS的思想计算相似度
    public static int gridLORS(List<String> tra1GridIDs, List<String> tra2GridIDs, int theta) {
        if (tra1GridIDs == null || tra2GridIDs == null || tra1GridIDs.size() == 0 || tra2GridIDs.size() == 0) {
            return 0;
        }

        int[][] dp = new int[tra1GridIDs.size()][tra2GridIDs.size()]; // dp数组
        int maxSimilarity = 0; // 相似度

        if (tra1GridIDs.get(0).equals(tra2GridIDs.get(0))) dp[0][0] = 1;

        for (int i = 1; i < tra1GridIDs.size(); i++) {
            if (tra1GridIDs.get(i).equals(tra2GridIDs.get(0))) {
                dp[i][0] = 1;
            } else {
                dp[i][0] = dp[i-1][0];
            }
        }

        for (int j = 1; j < tra2GridIDs.size(); j++) {
            if (tra2GridIDs.get(j).equals(tra1GridIDs.get(0))) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j-1];
            }
        }

        for (int i = 1; i < tra1GridIDs.size(); i++) {
            for (int j = 1; j < tra2GridIDs.size(); j++) {
                if (Math.abs(i - j) <= theta) {
                    if (tra1GridIDs.get(i).equals(tra2GridIDs.get(j))) {
                        dp[i][j] = 1 + dp[i-1][j-1];
                    } else {
                        dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                    }
                }

//                if (maxSimilarity < dp[i][j]) {
//                    maxSimilarity = dp[i][j];
//                }
            }
        }

        maxSimilarity = dp[tra1GridIDs.size() - 1][tra2GridIDs.size() - 1];
//        System.out.println("两条轨迹的相似度为：" + maxSimilarity);
        return maxSimilarity;
    }

    // 找到查询轨迹的topk条轨迹
    public static void topkWithLORS (String queryTraID, List<String> queryTraGridIDs, int k, List<String> R) {
        // 1. 找到经过【查询轨迹经过的grid】的所有轨迹作为candidates
        List<String> candidates = new ArrayList<>();
        HashMap<String, Integer> candidatesWithUpperBound = new HashMap<>();
        HashMap<String, List<String>> candidatesWithGrids = new HashMap<>();


        long filter_startTime = System.currentTimeMillis(); //获取开始时间

        // 2. 求出candidates中的每一条轨迹和查询轨迹的相似度上限UB
        // 遍历extendedGrids，获得所有需要扫描的轨迹
        for (int i = 0; i < queryTraGridIDs.size(); i++) {
            String gridid = queryTraGridIDs.get(i);
            List<String> tras = GTList.get(gridid);

            if (tras == null) continue;

            // 直接遍历轨迹，加入candidatesWithUB里
            for (String tra : tras) {
                List<String> grids = new ArrayList<>();
                grids.add(gridid);

                if (!candidates.contains(tra)) candidates.add(tra);
                if (candidatesWithGrids.get(tra) == null) {
                    candidatesWithGrids.put(tra, grids);
                } else {
                    grids = candidatesWithGrids.get(tra);
                    grids.add(gridid);
                    candidatesWithGrids.put(tra, grids);
                }
                candidatesWithUpperBound.merge(tra, 1, (oldValue, newValue) -> oldValue + newValue);
            }

        }

        long filter_endTime = System.currentTimeMillis(); //获取开始时间
        System.out.println("filter耗时：" + (filter_endTime - filter_startTime) + "ms");

        // 删除查询路径本身
        candidates.remove(queryTraID);
        candidatesWithGrids.remove(queryTraID);
        candidatesWithUpperBound.remove(queryTraID);

//        Set<String> set = new HashSet<>(); // 集合能去重
//        for (int i = 0; i < queryTraGridIDs.size(); i++) {
//            String gridid = queryTraGridIDs.get(i);
//            List<String> temp = GTList.get(gridid);
//            set.addAll(temp);
//        }
//        System.out.println(set);
//        candidates.addAll(set); // 集合转list
//        candidates.remove(queryTraID); // 删除查询路径本身
//
//        // 2. 求出candidates中的每一条轨迹和查询轨迹的相似度上限UB
//        for (int i = 0; i < candidates.size(); i++) {
//            String can = candidates.get(i);
//            List<String> canCubeIDs = TCList.get(can);
//            List<String> canGridIDs = new ArrayList<>();
//
//            for (int j = 0; j < canCubeIDs.size(); j++) {
//                String gid = ocTree.toGid(canCubeIDs.get(j), quadTree);
//                if (j > 0 && canGridIDs.get(canGridIDs.size() - 1).equals(gid)) continue;
//                canGridIDs.add(gid);
//            }
//
//            candidatesWithGrids.put(candidates.get(i), canGridIDs);
//
//            // 计算上限
//            List<String> queryTemp = new ArrayList<>(queryTraGridIDs);
//            List<String> canTemp = new ArrayList<>(canGridIDs);
//            int querySize = queryTemp.size();
//            queryTemp.removeAll(canTemp);
//            int UB = querySize - queryTemp.size();
//            candidatesWithUpperBound.put(candidates.get(i), UB);
//        }

        // 3. 根据UB从大到小给candidates排序
        // key for trajectory id, value for its upper bound            poll从大到小
        PriorityQueue<Map.Entry<String, Integer>> upperBoundRank = new PriorityQueue<>((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        upperBoundRank.addAll(candidatesWithUpperBound.entrySet());

        // key for trajectory id, value for its exact score            poll从小到大
        PriorityQueue<Map.Entry<String, Integer>> topKHeap = new PriorityQueue<>(Map.Entry.comparingByValue());
        double bestKth = -Integer.MAX_VALUE;

        long refine_startTime = System.currentTimeMillis(); //获取开始时间

        // 4. 依次求出candidates和查询轨迹的真实相似度
        // 5. 如果R中的第k条候选轨迹比剩下未计算真实值的candidates的最大UB大，直接返回R
        while (!upperBoundRank.isEmpty()) {
            Map.Entry<String, Integer> entry = upperBoundRank.poll();

            if (topKHeap.size() >= k && bestKth > entry.getValue()) break; // early termination

            List<String> candidateWithGrids = candidatesWithGrids.get(entry.getKey()); // 本身是有先后顺序的

            int exactValue = gridLORS(queryTraGridIDs, candidateWithGrids, theta);

            entry.setValue(exactValue);
            topKHeap.add(entry);
            if (topKHeap.size() > k) topKHeap.poll();

            bestKth = topKHeap.peek().getValue();
        }

        long refine_endTime = System.currentTimeMillis(); //获取开始时间
        System.out.println("refine耗时：" + (refine_endTime - refine_startTime) + "ms");

        while (topKHeap.size() > 0) {
            R.add(topKHeap.poll().getKey());
        }



//        System.out.println(R);
//        return;
    }

    //==========================================================LCSS==========================================================================================================
    // 网格id列表->获得网格坐标列表
    public static void gridToXYZ(List<String> tra1GridIDs, List<String> tra2GridIDs, List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        for (int i = 0; i < tra1GridIDs.size(); i++) {
            Point2D point = quadtree.getBoundingBox(tra1GridIDs.get(i)).getMin();
            tra1GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
        }
        for (int i = 0; i < tra2GridIDs.size(); i++) {
            Point2D point = quadtree.getBoundingBox(tra2GridIDs.get(i)).getMin();
            tra2GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
        }
    }

    // 计算两点之间的欧氏距离
    public static double calDistance(Point2D tra1GridPoint, Point2D tra2GridPoint) {
        return Math.sqrt((tra1GridPoint.getX()-tra2GridPoint.getX())*(tra1GridPoint.getX()-tra2GridPoint.getX()) + (tra1GridPoint.getY()-tra2GridPoint.getY())*(tra1GridPoint.getY()-tra2GridPoint.getY()));
    }

    // 把cubes转化成对应的grids然后用LCSS的思想计算相似度
    public static double gridLCSS(List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        if (tra1GridPoint == null || tra2GridPoint == null || tra1GridPoint.size() == 0 || tra2GridPoint.size() == 0) {
            return 0;
        }

        int[][] dpInts = new int[tra1GridPoint.size()][tra2GridPoint.size()];

        Comparator<Point2D> comparator = (p1, p2) -> {
            double dist = calDistance(p1, p2);
            if (dist <= lambda) return 0;
            return 1;
        };


        if (comparator.compare(tra1GridPoint.get(0), tra2GridPoint.get(0)) == 0) dpInts[0][0] = 1;

        for (int i = 1; i < tra1GridPoint.size(); i++) {
            if (comparator.compare(tra1GridPoint.get(i), tra2GridPoint.get(0)) == 0) {
                dpInts[i][0] = 1;
            } else {
                dpInts[i][0] = dpInts[i - 1][0];
            }
        }

        for (int i = 1; i < tra2GridPoint.size(); i++) {
            if (comparator.compare(tra2GridPoint.get(i), tra1GridPoint.get(0)) == 0)
                dpInts[0][i] = 1;
            else dpInts[0][i] = dpInts[0][i - 1];
        }

        for (int i = 1; i < tra1GridPoint.size(); i++) {
            for (int j = 1; j < tra2GridPoint.size(); j++) {
                if (Math.abs(i - j) <= theta) {
                    if (comparator.compare(tra1GridPoint.get(i), tra2GridPoint.get(j)) == 0) {
                        dpInts[i][j] = 1 + dpInts[i - 1][j - 1];
                    } else {
                        dpInts[i][j] = Math.max(dpInts[i - 1][j], dpInts[i][j - 1]);
                    }
                }
            }
        }

        double result = dpInts[tra1GridPoint.size() - 1][tra2GridPoint.size() - 1] * 1.0;
//        double similarity = 1 - result / Math.min(tra1GridPoint.size(), tra2GridPoint.size());
        return result;
    }

    // 找到查询轨迹的topk条轨迹
    public static void topkWithLCSS (String queryTraID, List<String> queryTraGridIDs, int k, List<String> R) {
        // 1. 找到经过【查询轨迹经过的grid及与该grid距离小于lambda的grid】的所有轨迹作为candidates
        List<String> candidates = new ArrayList<>();
        HashMap<String, Double> candidatesWithUpperBound = new HashMap<>();
        HashMap<String, List<String>> candidatesWithGrids = new HashMap<>();

        List<String> extendedGrids = new ArrayList<>();



        // 使用接口获得所有的grid
        Set<String> set1 = new HashSet<>();
        for (int i = 0; i < queryTraGridIDs.size(); i++) {
            String gridid = queryTraGridIDs.get(i);
            // 使用接口获取一定范围内的grid
            ArrayList<String> temp = quadtree.rangeQueryByRadius(adjList, gridid, lambda);
//            System.out.println("temp" + temp);
            set1.addAll(temp);
        }
        extendedGrids.addAll(set1);

        long filter_startTime = System.currentTimeMillis(); //获取开始时间

        // 2. 求出candidates中的每一条轨迹和查询轨迹的相似度上限UB
        // 遍历extendedGrids，获得所有需要扫描的轨迹
        for (int i = 0; i < extendedGrids.size(); i++) {
            String gridid = extendedGrids.get(i);
            List<String> tras = GTList.get(gridid);

            if (tras == null) continue;

            // 直接遍历轨迹，加入candidatesWithUB里
            for (String tra : tras) {
                List<String> grids = new ArrayList<>();
                grids.add(gridid);

                if (!candidates.contains(tra)) candidates.add(tra);
                if (candidatesWithGrids.get(tra) == null) {
                    candidatesWithGrids.put(tra, grids);
                } else {
                    grids = candidatesWithGrids.get(tra);
                    grids.add(gridid);
                    candidatesWithGrids.put(tra, grids);
                }
                candidatesWithUpperBound.merge(tra, 1.0, (oldValue, newValue) -> oldValue + newValue);
            }


        }

        long filter_endTime = System.currentTimeMillis(); //获取开始时间
        System.out.println("filter耗时：" + (filter_endTime - filter_startTime) + "ms");

        // 删除查询路径本身
        candidates.remove(queryTraID);
        candidatesWithGrids.remove(queryTraID);
        candidatesWithUpperBound.remove(queryTraID);

        // 3. 根据UB从大到小给candidates排序
        // key for trajectory id, value for its upper bound            poll从大到小
        PriorityQueue<Map.Entry<String, Double>> upperBoundRank = new PriorityQueue<>((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        upperBoundRank.addAll(candidatesWithUpperBound.entrySet());

        // key for trajectory id, value for its exact score            poll从小到大
        PriorityQueue<Map.Entry<String, Double>> topKHeap = new PriorityQueue<>(Map.Entry.comparingByValue());
        double bestKth = -Integer.MAX_VALUE;

        long refine_startTime = System.currentTimeMillis(); //获取开始时间
        // 4. 依次求出candidates和查询轨迹的真实相似度
        // 5. 如果R中的第k条候选轨迹比剩下未计算真实值的candidates的最大UB大，直接返回R
        while (!upperBoundRank.isEmpty()) {
            Map.Entry<String, Double> entry = upperBoundRank.poll();

            if (topKHeap.size() >= k && bestKth > entry.getValue()) break; // early termination

            List<String> candidateWithGrids = candidatesWithGrids.get(entry.getKey()); // 本身是有先后顺序的

            List<Point2D> queryGridPoint = new ArrayList<>();
            List<Point2D> candidateGridPoint = new ArrayList<>();
            gridToXYZ(queryTraGridIDs, candidateWithGrids, queryGridPoint, candidateGridPoint);

            double exactValue = gridLCSS(queryGridPoint, candidateGridPoint);

            entry.setValue(exactValue);
            topKHeap.add(entry);
            if (topKHeap.size() > k) topKHeap.poll();

            bestKth = topKHeap.peek().getValue();
        }

        long refine_endTime = System.currentTimeMillis(); //获取开始时间
        System.out.println("refine耗时：" + (refine_endTime - refine_startTime) + "ms");

        while (topKHeap.size() > 0) {
            R.add(topKHeap.poll().getKey());
        }

//        System.out.println(R);
//        return;
    }

    //=========================================================Frechet========================================================================================================

    // 把cubes转化成对应的grids然后用Frechet的思想计算相似度
    // 结果越小说明相似度越高
    public static double gridFrechet(List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        if (tra1GridPoint == null || tra2GridPoint == null || tra1GridPoint.size() == 0 || tra2GridPoint.size() == 0) {
            return 0;
        }

        double[][] ca = new double[tra1GridPoint.size()][tra2GridPoint.size()];
        for (int i = 0; i < tra1GridPoint.size(); i++) {
            for ( int j = 0; j < tra2GridPoint.size(); j++) {
                ca[i][j] = -1.0D;
            }
        }

        return c(tra1GridPoint.size() - 1, tra2GridPoint.size() - 1, ca, tra1GridPoint, tra2GridPoint);
    }

    // 递归
    public static double c(int i, int j, double[][] ca, List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        if (ca[i][j] > -1.0D) return ca[i][j];

        if (i == 0 && j == 0) {
            ca[i][j] = calDistance(tra1GridPoint.get(0), tra2GridPoint.get(0));
        } else if (i > 0 && j == 0) {
            ca[i][j] = Math.max(c(i - 1, 0, ca, tra1GridPoint, tra2GridPoint), calDistance(tra1GridPoint.get(i), tra2GridPoint.get(0)));
        } else if (i == 0 && j > 0) {
            ca[i][j] = Math.max(c(0, j - 1, ca, tra1GridPoint, tra2GridPoint), calDistance(tra1GridPoint.get(0), tra2GridPoint.get(j)));
        } else if (i > 0 && j > 0){
            ca[i][j] = Math.max(Math.min(Math.min(c(i - 1, j, ca, tra1GridPoint, tra2GridPoint), c(i - 1, j - 1, ca, tra1GridPoint, tra2GridPoint)), c(i, j - 1, ca, tra1GridPoint, tra2GridPoint)), calDistance(tra1GridPoint.get(i), tra2GridPoint.get(j)));
        }

        return ca[i][j];
    }

    //===========================================================Hausdorff===================================================================================================
    // 把cubes转化成对应的grids然后用Hausdorff的思想计算相似度
    // 结果越小相似度越高
    public static double gridHausdorff(List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        if (tra1GridPoint == null || tra2GridPoint == null || tra1GridPoint.size() == 0 || tra2GridPoint.size() == 0) {
            return 0;
        }

        // 点与点之间的距离矩阵
        double[][] dist_matrix = new double[tra1GridPoint.size()][tra2GridPoint.size()];
        double distance = 0; // 相似度

        List<Double> minDistance1 = new ArrayList<>(); // tra1的点到tra2的点的最小距离集合
        List<Double> minDistance2 = new ArrayList<>();

        for (int i = 0; i < tra1GridPoint.size(); i++) {
            for (int j = 0; j < tra2GridPoint.size(); j++) {
                dist_matrix[i][j] = calDistance(tra1GridPoint.get(i), tra2GridPoint.get(j));
            }
        }

        // tra1的点到tra2的点的最小距离集合
        for (int i = 0; i < tra1GridPoint.size(); i++) {

            double min = Double.MAX_VALUE;

            for (int j = 0; j < tra2GridPoint.size(); j++) {
                if (dist_matrix[i][j] <= min) {
                    min = dist_matrix[i][j];
                }
            }

            minDistance1.add(min);
        }

        // tra2的点到tra1的点的最小距离集合
        for (int i = 0; i < tra2GridPoint.size(); i++) {

            double min = Double.MAX_VALUE;

            for (int j = 0; j < tra1GridPoint.size(); j++) {
                if (dist_matrix[j][i] <= min) {
                    min = dist_matrix[j][i];
                }
            }

            minDistance2.add(min);
        }

        Collections.sort(minDistance1);
        Collections.sort(minDistance2);
        double value1 = minDistance1.get(minDistance1.size() - 1);
        double value2 = minDistance2.get(minDistance2.size() - 1);
        distance = Math.max(value1, value2);

//        System.out.println("两条轨迹的Hausdorff距离为：" + distance);
        return distance;
    }

    //=====================================================Hausdorff和Frechet================================================================================================
    // topk
    public static void topkWithFrechetOrHausdorff (String queryTraID, List<String> queryTraGridIDs, int k, List<String> R, String horf) {
        // poll()出来的元素是从小到大
        PriorityQueue<Map.Entry<String, Double>> topKHeap = new PriorityQueue<>(Comparator.comparingDouble(p -> p.getValue()));
        double bestKthSoFar = - Double.MAX_VALUE; //目前第k个值
        double overallUnseenedUpperBound;         // 未被扫描到的轨迹的UB
        double[] unseenUpperBounds = new double[queryTraGridIDs.size()]; // 未被扫描到的UB数组

        // expansion轮数
        int round = INITIAL_ROUND_FOR_H_OR_F;
        Set<String> visitTrajectorySet = new HashSet<>();

        int check = 0;
        while (check == 0) {
//            System.out.println("Round" + round);
            overallUnseenedUpperBound  = Double.MAX_VALUE;

            // 每条轨迹的相似度upperbound
            HashMap<String, Double> trajUpperBound = new HashMap<>();
            // 每条轨迹和到查询轨迹每个grid的最小值
            HashMap<String, HashMap<String, Double>> trajUpperBoundDetailed = new HashMap<>();


            // 对查询轨迹经过的grid逐个进行扩张
            // 算出未被扫描到的轨迹的最大近似值UB
            // 算出经过扩张范围轨迹的UB以及它们离查询轨迹经过的grid的最近距离的具体信息
            int querySize = queryTraGridIDs.size();
            for (int i = 0; i < querySize; i++) {

                String queryGridID = queryTraGridIDs.get(i);

                List<String> grids = new ArrayList<>();
                // 取出扫描到的grids
                if(round == 1) {
                    HashSet<String> res = quadtree.expandByAdjList(adjList, queryGridID,1);
                    grids = new ArrayList<>(res);
                    grids.add(queryGridID);
                } else {
                    HashSet<String> res1 = quadtree.expandByAdjList(adjList, queryGridID,round+1);
                    HashSet<String> res2 = quadtree.expandByAdjList(adjList, queryGridID,round);

                    HashSet<String> res = new HashSet<>();
                    res.addAll(res1);
                    res.removeAll(res2);
                    grids = new ArrayList<>(res);
                }

                // 这里计算的和论文里的不一样，直接用的网格之间的距离
                // unseenUB是查询轨迹每个网格扩张时的UB
//                double upperBound = - round * (quadTree.getBoundingBox(queryGridID).getMax().getX() - quadTree.getBoundingBox(queryGridID).getMin().getX()) * Math.sqrt(2);

                double upperBound = Double.MAX_VALUE;
                for(String grid : grids)

                unseenUpperBounds[i] = upperBound;
                overallUnseenedUpperBound = Math.min(upperBound, overallUnseenedUpperBound);



                for (String grid : grids) {
                    // 计算两个网格之间的距离
//                    Point2D queryGridPoint = quadtree.getBoundingBox(queryGridID).getMin();
//                    Point2D currentGridPoint = quadtree.getBoundingBox(grid).getMin();
                    // 注意是负数
                    Double score = - quadtree.getDistance(queryGridID, grid);
                    if(score < upperBound) upperBound = score;

                    // 取出经过这个网格的轨迹
                    List<String> tras = GTList.get(grid);
                    if (tras == null) continue;
                    if (tras.contains(queryTraID)) tras.remove(queryTraID);
                    for (String tra : tras) {
                        // 这里map和trajUBD里的对应的map指向的同一片区域，改变这个map就能改变原map
                        HashMap<String, Double> map = trajUpperBoundDetailed.get(tra);
                        if (map != null) {
                            if (!map.containsKey(queryGridID) || score > map.get(queryGridID)) map.put(queryGridID, score);
                        } else {
                            map = trajUpperBoundDetailed.computeIfAbsent(tra, key -> new HashMap<>());
                            map.put(queryGridID, score);
                        }
                    }
                }
            }

            // 遍历目前扩张范围内查找到的轨迹
            for (HashMap.Entry<String, HashMap<String, Double>> entry: trajUpperBoundDetailed.entrySet()) {
                String trajID = entry.getKey();

                // trajectory对应的grid和score
                HashMap<String, Double> map = entry.getValue();
                double score = Double.MAX_VALUE;
                // 遍历查询轨迹经过的grid
                for (int i = 0; i < querySize; i++) {
                    String curGrid = queryTraGridIDs.get(i);
                    if (map.containsKey(curGrid)) {
                        score = Math.min(map.get(curGrid), score);
                    } else {
                        score = Math.min(unseenUpperBounds[i], score);
                    }
                }
                // 找到每条轨迹到查询轨迹的负数最小距离——即最大距离
                trajUpperBound.put(trajID, score);
            }


            // 根据轨迹的UB排序
            PriorityQueue<Map.Entry<String, Double>> rankedCandidates = new PriorityQueue<>((e1,e2) -> Double.compare(e2.getValue(),e1.getValue()));

            for (Map.Entry<String, Double> entry : trajUpperBound.entrySet()) {
                if (!visitTrajectorySet.contains(entry.getKey()))
                    rankedCandidates.add(entry);
            }

            // 标记已经访问过的轨迹
            visitTrajectorySet.addAll(trajUpperBound.keySet());

            long refine_startTime = System.currentTimeMillis(); //获取开始时间

            // 计算每条轨迹的准确的距离
            int j = 0;
            while (!rankedCandidates.isEmpty()) {

                Map.Entry<String, Double> entry1 = rankedCandidates.poll();
                String curTrajId = entry1.getKey();
                double curUpperBound = entry1.getValue();

                // 获取该轨迹经过的网格
                List<String> canCubeIDs = TCList.get(curTrajId);
                List<String> canGridIDs = new ArrayList<>();

                for (int i = 0; i < canCubeIDs.size(); i++) {
                    String gid = octree.toGid(canCubeIDs.get(i), quadtree);
                    if (i > 0 && canGridIDs.get(canGridIDs.size() - 1).equals(gid)) continue;
                    canGridIDs.add(gid);
                }

                if (canGridIDs == null) continue;
                double realDist = 0;

                List<Point2D> queryGridPoint = new ArrayList<>();
                List<Point2D> candidateGridPoint = new ArrayList<>();
                gridToXYZ(queryTraGridIDs, canGridIDs, queryGridPoint, candidateGridPoint);

//                System.out.println(curTrajId);
                if (horf.equals("h")) {
                    realDist = gridHausdorff(queryGridPoint, candidateGridPoint);
                } else {
                    realDist = gridFrechet(queryGridPoint, candidateGridPoint);
                }

                // 分数将真实结果取负数
                double score = -realDist;
                entry1.setValue(score);

                if (topKHeap.size() < k) {
                    topKHeap.add(entry1);
                } else {
                    if (topKHeap.peek().getValue() < score) {
                        topKHeap.add(entry1);
                        topKHeap.poll();
                    }

                    bestKthSoFar = topKHeap.peek().getValue();

                    if (bestKthSoFar > overallUnseenedUpperBound) check = 1;

                    if (bestKthSoFar > curUpperBound) break;

                }
            }

            round++;
        }


        List<String> resIDList = new ArrayList<>();
        while (!topKHeap.isEmpty()) {
            resIDList.add(topKHeap.poll().getKey());
        }

//        System.out.println(resIDList);

    }
}
