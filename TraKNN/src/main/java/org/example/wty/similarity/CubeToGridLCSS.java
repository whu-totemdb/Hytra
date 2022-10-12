package org.example.wty.similarity;

import cn.edu.whu.index.*;
import cn.edu.whu.index.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CubeToGridLCSS {

    static int maxLevel; // 构造八叉树和四叉树的最大层数
    public static List<String> R = new ArrayList<>(); // topk结果集
    static int theta = 1000;
    static int lambda = 100;

    // 三个倒排表
    static HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
    static HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
    static HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs

    // 八叉树和四叉树
    static BoundingBox3D root3D = null;
    static OcTree ocTree = null;
    static BoundingBox2D root2D = null;
    static QuadTree quadTree = null;


    // 1. 构建八叉树和四叉树
    // 2. 构建CTList、TCList、GTList
    public static void readTxtFile(String filePath, HashMap<String, List> TCList,  HashMap<String, List> CTList,  HashMap<String, List> GTList, double[] min_xyz, double[] max_xyz){
        try {
            long startTime = System.currentTimeMillis(); //获取开始时间
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);


                int lines = 0;
                int start = 4;
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
//                while(lines < 33003){
//                    lineTxt = bufferedReader.readLine();
                    lines++;
                    if (lines == 1) {
                        String min = lineTxt.substring(lineTxt.indexOf("(") + 1, lineTxt.indexOf(")")); // 取出三个坐标
                        String[] xyz = min.split(", ");
                        for (int i = 0; i < xyz.length; i++) {
                            Double coor = Double.parseDouble(xyz[i]);
                            min_xyz[i] = coor;
//                            System.out.println("坐标们：" + min_xyz[i]);
                        }
                    }
                    if (lines == 2) {
                        String max = lineTxt.substring(lineTxt.indexOf("(") + 1, lineTxt.indexOf(")")); // 取出三个坐标
                        String[] xyz = max.split(", ");
                        for (int i = 0; i < xyz.length; i++) {
                            Double coor = Double.parseDouble(xyz[i]);
                            max_xyz[i] = coor;
//                            System.out.println("坐标们：" + max_xyz[i]);
                        }
                    }
                    if (lines == 3) {
                        String level = lineTxt.substring(lineTxt.indexOf("=") + 1);
                        maxLevel = Integer.parseInt(level);
//                        System.out.println("level：" + maxLevel);

                        double min_x = min_xyz[0];
                        double min_y = min_xyz[1];
                        double min_z = min_xyz[2];
                        double max_x = max_xyz[0];
                        double max_y = max_xyz[1];
                        double max_z = max_xyz[2];
                        root3D = new BoundingBox3D(new Point3D(min_x, min_y, min_z), new Point3D(max_x, max_y, max_z));
                        ocTree = new OcTree(root3D, maxLevel, "2022-03-11", 10);
                        root2D = new BoundingBox2D(new Point2D(min_x, min_y), new Point2D(max_x, max_y));
                        quadTree = new QuadTree(root2D, maxLevel, "2022-03-11");
                    }
                    if (lines >= start) {
                        String traID = lineTxt.substring(0, lineTxt.indexOf("=")); //取出traid字符串
                        String cubeID = lineTxt.substring(lineTxt.indexOf("[") + 1, lineTxt.indexOf("]")); // 取出cubeids字符串

                        List<String> cubeIDs = new ArrayList<>(); // 打算把cube的id放到一个list里
//                        System.out.println(cubeID);
//                        System.out.println(cubeID.indexOf(","));

                        if (cubeID.indexOf(",") == -1) { // 如果只有一个cubid
//                            cubeID = cubeID.substring(11);
//                            cubeID = cubeID.substring(0, cubeID.indexOf("@"));
//                            cubeIDs.add(cubeID);
                            cubeIDs.add(cubeID);
                        } else {
                            String[] temp = cubeID.split(", ");

                            for (int i = 0; i < temp.length; i++) {
//                                String cid = temp[i].substring(11);
//                                cid = cid.substring(0, cid.indexOf("@"));
//                                cubeIDs.add(cid);
                                cubeIDs.add(temp[i]);
                            }
                        }

                        // 构建tid-cid的hashmap
                        TCList.put(traID, cubeIDs);

                        // 构建cid-tid的hashmap和gid-tid的hashmap
                        for (int i = 0; i < cubeIDs.size(); i++) {
                            List<String> cubetraIDs = new ArrayList<>();
                            List<String> gridtraIDs = new ArrayList<>();
                            String cubeid = cubeIDs.get(i);
                            String gridid = ocTree.toGid(cubeid, quadTree);

                            // 构建CTList
                            if (CTList.get(cubeid) == null) {
                                cubetraIDs.add(traID);
                                CTList.put(cubeid, cubetraIDs);
                            } else {
                                cubetraIDs = CTList.get(cubeid);
                                cubetraIDs.add(traID);
                                CTList.put(cubeid, cubetraIDs);
                            }

                            // 构建GTList
                            if (GTList.get(gridid) == null) {
                                gridtraIDs.add(traID);
                                GTList.put(gridid, gridtraIDs);
                            } else {
                                gridtraIDs = GTList.get(gridid);
                                if (!gridtraIDs.contains(traID)) {
                                    gridtraIDs.add(traID);
                                    GTList.put(gridid, gridtraIDs);
                                }

                            }

                        }

                    }
                }
                read.close();
                long endTime = System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间：" + (endTime - startTime) + "ms");

//                Iterator iter1 = GTList.entrySet().iterator();
//                int order1 = 0;
//                while (iter1.hasNext()) {
//                    order1++;
//                    Map.Entry entry = (Map.Entry) iter1.next();
//                    Object key = entry.getKey();
//                    Object value = entry.getValue();
//                    System.out.println("网格" + order1 + ":" + key + "  轨迹：" + value);
//                }

            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

    // 网格id列表->获得网格坐标列表
    public static void gridToXYZ(List<String> tra1GridIDs, List<String> tra2GridIDs, List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
        for (int i = 0; i < tra1GridIDs.size(); i++) {
            Point2D point = quadTree.getBoundingBox(tra1GridIDs.get(i)).getMin();
            tra1GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
        }
        for (int i = 0; i < tra2GridIDs.size(); i++) {
            Point2D point = quadTree.getBoundingBox(tra2GridIDs.get(i)).getMin();
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
//            List<String> temp = quadTree.rangeQueryByRadius(gridid, lambda);
//            System.out.println("temp" + temp);
            // TODO:接口
            List<String> temp = new ArrayList<>();
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

    public static void main(String[] args) {
        // 数据集文件路径
        String filePath = "./src/main/java/org/example/wty/similarity/6.txt";

        // cube的最小最大坐标
        // x y z
        double[] min_xyz = new double[3];
        double[] max_xyz = new double[3];

//        // 八叉树和四叉树
//        BoundingBox3D root3D = null;
//        OcTree ocTree = null;
//        BoundingBox2D root2D = null;
//        QuadTree quadTree = null;

//        // 三个倒排表
//        HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
//        HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
//        HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs

        // 读取数据集文件
        readTxtFile(filePath, TCList, CTList, GTList, min_xyz, max_xyz);

        // 构造八叉树和四叉树
//        double min_x = min_xyz[0];
//        double min_y = min_xyz[1];
//        double min_z = min_xyz[2];
//        double max_x = max_xyz[0];
//        double max_y = max_xyz[1];
//        double max_z = max_xyz[2];
//        BoundingBox3D root3D = new BoundingBox3D(new Point3D(min_x, min_y, min_z), new Point3D(max_x, max_y, max_z));
//        OcTree ocTree = new OcTree(root3D, maxLevel, "2022-03-11", 10);
//        BoundingBox2D root2D = new BoundingBox2D(new Point2D(min_x, min_y), new Point2D(max_x, max_y));
//        QuadTree quadTree = new QuadTree(root2D, maxLevel, "2022-03-11");


//        // ------------------------------------计算两条轨迹相似度--------------------------------------------
//        //创建输入对象
//        Scanner sc = new Scanner(System.in);
//
//        //list存储用户输入的字符串
//        List<String> queryTraIDs = new ArrayList<>();
//
//        //存储轨迹id经过的cubes
//        List<String> tra1CubeIDs = new ArrayList<>();
//        List<String> tra2CubeIDs = new ArrayList<>();
//
//
//        System.out.print("请输入轨迹id：" + "\n");
//        String nextLine = sc.nextLine();
//        while (nextLine != null && !nextLine.equals("")) {
////            System.out.println(nextLine);
//            queryTraIDs.add(nextLine);
//            if(queryTraIDs.size() == 2) break;
//            nextLine = sc.nextLine();
//        }
//
//        for(int i = 0; i < queryTraIDs.size(); i++) {
//            String traid = queryTraIDs.get(i);
//            System.out.println("输入的轨迹" + (i+1) + "的ID：" + traid);
//            if(i == 0) tra1CubeIDs = TCList.get(traid);
//            if(i == 1) tra2CubeIDs = TCList.get(traid);
//        }
//
//        List<String> tra1GridIDs = new ArrayList<>();
//        List<String> tra2GridIDs = new ArrayList<>();
//        for (int i = 0; i < tra1CubeIDs.size(); i++){
//            String gid1 = ocTree.toGid(tra1CubeIDs.get(i), quadTree);
////            System.out.println("cubeid:" + tra1CubeIDs.get(i));
//            if (i > 0 && tra1GridIDs.get(tra1GridIDs.size() - 1).equals(gid1)) continue;
//            tra1GridIDs.add(gid1);
//        }
//        for (int i = 0; i < tra2CubeIDs.size(); i++){
//            String gid2 = ocTree.toGid(tra2CubeIDs.get(i), quadTree);
//            if (i > 0 && tra2GridIDs.get(tra2GridIDs.size() - 1).equals(gid2)) continue;
//            tra2GridIDs.add(gid2);
//        }
//
//        // 计算两个grid之间的距离时，统一使用左下方的点，即计算两个网格左下角点的距离
//        // 以下两个list存储轨迹经过的grid的左下角的点
//        List<Point2D> tra1GridPoint = new ArrayList<>();
//        List<Point2D> tra2GridPoint = new ArrayList<>();
//        gridToXYZ(tra1GridIDs, tra2GridIDs, tra1GridPoint, tra2GridPoint);
////        for (int i = 0; i < tra1GridIDs.size(); i++) {
////            Point2D point = quadTree.getBoundingBox(tra1GridIDs.get(i)).getMin();
////            tra1GridPoint.add(point);
////            System.out.println(point.getX() + ", " + point.getY());
////        }
////        for (int i = 0; i < tra2GridIDs.size(); i++) {
////            Point2D point = quadTree.getBoundingBox(tra2GridIDs.get(i)).getMin();
////            tra2GridPoint.add(point);
////            System.out.println(point.getX() + ", " + point.getY());
////        }
////
//        double sim = gridLCSS(tra1GridPoint, tra2GridPoint);
//        // ----------------------------------计算完成----------------------------------------------------

        // ----------------------------------输入一条查询轨迹计算它的topk----------------------------------------------------
        // 创建输入对象
        Scanner sc = new Scanner(System.in);

        // 获取用户输入的字符串
        String queryTraID = null;
        System.out.print("请输入轨迹id：");
        queryTraID = sc.nextLine();

        System.out.println("输入的轨迹id：" + queryTraID);

        // 存储轨迹id经过的cubes
        List<String> queryTraCubeIDs = new ArrayList<>();
        queryTraCubeIDs = TCList.get(queryTraID);
//        System.out.println(queryTraCubeIDs);

        // 存储轨迹id经过的grids
        List<String> queryTraGridIDs = new ArrayList<>();

        for (int i = 0; i < queryTraCubeIDs.size(); i++){
            String gid = ocTree.toGid(queryTraCubeIDs.get(i), quadTree);
            if (i > 0 && queryTraGridIDs.get(queryTraGridIDs.size() - 1).equals(gid)) continue;
            queryTraGridIDs.add(gid);
        }

        // topk的k
        int k = 20;
        // 进行topk查询
        long startTime = System.currentTimeMillis(); //获取开始时间
        topkWithLCSS(queryTraID, queryTraGridIDs, k, R);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Top-k查询时间：" + (endTime - startTime) + "ms");
        // -------------------------------------------topk查询结束--------------------------------------------------------
    }
}
