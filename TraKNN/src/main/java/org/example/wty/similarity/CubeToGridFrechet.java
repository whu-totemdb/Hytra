//package org.example.wty.similarity;
//
//import cn.edu.whu.index.*;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.util.*;
//
//public class CubeToGridFrechet {
//    static int maxLevel; // 构造八叉树和四叉树的最大层数
//    public static List<String> R = new ArrayList<>(); // topk结果集
//    static int theta = 1000;
//    static int lambda = 50;
//
//    // 三个倒排表
//    static HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
//    static HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
//    static HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs
//
//    // 八叉树和四叉树
//    static BoundingBox3D root3D = null;
//    static OcTree ocTree = null;
//    static BoundingBox2D root2D = null;
//    static QuadTree quadTree = null;
//
//
//    // 1. 构建八叉树和四叉树
//    // 2. 构建CTList、TCList、GTList
//    public static void readTxtFile(String filePath, HashMap<String, List> TCList,  HashMap<String, List> CTList,  HashMap<String, List> GTList, double[] min_xyz, double[] max_xyz){
//        try {
//            long startTime = System.currentTimeMillis(); //获取开始时间
//            String encoding="GBK";
//            File file=new File(filePath);
//            if(file.isFile() && file.exists()){ //判断文件是否存在
//                InputStreamReader read = new InputStreamReader(
//                        new FileInputStream(file),encoding);//考虑到编码格式
//                BufferedReader bufferedReader = new BufferedReader(read);
//
//
//                int lines = 0;
//                int start = 4;
//                String lineTxt = null;
//                while((lineTxt = bufferedReader.readLine()) != null){
//                    lines++;
//                    if (lines == 1) {
//                        String min = lineTxt.substring(lineTxt.indexOf("(") + 1, lineTxt.indexOf(")")); // 取出三个坐标
//                        String[] xyz = min.split(", ");
//                        for (int i = 0; i < xyz.length; i++) {
//                            Double coor = Double.parseDouble(xyz[i]);
//                            min_xyz[i] = coor;
//                            System.out.println("坐标们：" + min_xyz[i]);
//                        }
//                    }
//                    if (lines == 2) {
//                        String max = lineTxt.substring(lineTxt.indexOf("(") + 1, lineTxt.indexOf(")")); // 取出三个坐标
//                        String[] xyz = max.split(", ");
//                        for (int i = 0; i < xyz.length; i++) {
//                            Double coor = Double.parseDouble(xyz[i]);
//                            max_xyz[i] = coor;
//                            System.out.println("坐标们：" + max_xyz[i]);
//                        }
//                    }
//                    if (lines == 3) {
//                        String level = lineTxt.substring(lineTxt.indexOf("=") + 1);
//                        maxLevel = Integer.parseInt(level);
//                        System.out.println("level：" + maxLevel);
//
//                        double min_x = min_xyz[0];
//                        double min_y = min_xyz[1];
//                        double min_z = min_xyz[2];
//                        double max_x = max_xyz[0];
//                        double max_y = max_xyz[1];
//                        double max_z = max_xyz[2];
//                        root3D = new BoundingBox3D(new Point3D(min_x, min_y, min_z), new Point3D(max_x, max_y, max_z));
//                        ocTree = new OcTree(root3D, maxLevel, "2022-03-11", 10);
//                        root2D = new BoundingBox2D(new Point2D(min_x, min_y), new Point2D(max_x, max_y));
//                        quadTree = new QuadTree(root2D, maxLevel, "2022-03-11");
//                    }
//                    if (lines >= start) {
//                        String traID = lineTxt.substring(0, lineTxt.indexOf("=")); //取出traid字符串
//                        String cubeID = lineTxt.substring(lineTxt.indexOf("[") + 1, lineTxt.indexOf("]")); // 取出cubeids字符串
//
//                        List<String> cubeIDs = new ArrayList<>(); // 打算把cube的id放到一个list里
////                        System.out.println(cubeID);
////                        System.out.println(cubeID.indexOf(","));
//
//                        if (cubeID.indexOf(",") == -1) { // 如果只有一个cubid
////                            cubeID = cubeID.substring(11);
////                            cubeID = cubeID.substring(0, cubeID.indexOf("@"));
////                            cubeIDs.add(cubeID);
//                            cubeIDs.add(cubeID);
//                        } else {
//                            String[] temp = cubeID.split(", ");
//
//                            for (int i = 0; i < temp.length; i++) {
////                                String cid = temp[i].substring(11);
////                                cid = cid.substring(0, cid.indexOf("@"));
////                                cubeIDs.add(cid);
//                                cubeIDs.add(temp[i]);
//                            }
//                        }
//
//                        // 构建tid-cid的hashmap
//                        TCList.put(traID, cubeIDs);
//
//                        // 构建cid-tid的hashmap和gid-tid的hashmap
//                        for (int i = 0; i < cubeIDs.size(); i++) {
//                            List<String> cubetraIDs = new ArrayList<>();
//                            List<String> gridtraIDs = new ArrayList<>();
//                            String cubeid = cubeIDs.get(i);
//                            String gridid = ocTree.toGid(cubeid, quadTree);
//
//                            // 构建CTList
//                            if (CTList.get(cubeid) == null) {
//                                cubetraIDs.add(traID);
//                                CTList.put(cubeid, cubetraIDs);
//                            } else {
//                                cubetraIDs = CTList.get(cubeid);
//                                cubetraIDs.add(traID);
//                                CTList.put(cubeid, cubetraIDs);
//                            }
//
//                            // 构建GTList
//                            if (GTList.get(gridid) == null) {
//                                gridtraIDs.add(traID);
//                                GTList.put(gridid, gridtraIDs);
//                            } else {
//                                gridtraIDs = GTList.get(gridid);
//                                if (!gridtraIDs.contains(traID)) {
//                                    gridtraIDs.add(traID);
//                                    GTList.put(gridid, gridtraIDs);
//                                }
//
//                            }
//
//                        }
//
//                    }
//                }
//                read.close();
//                long endTime = System.currentTimeMillis(); //获取结束时间
//                System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
//
//                Iterator iter1 = GTList.entrySet().iterator();
//                int order1 = 0;
//                while (iter1.hasNext()) {
//                    order1++;
//                    Map.Entry entry = (Map.Entry) iter1.next();
//                    Object key = entry.getKey();
//                    Object value = entry.getValue();
//                    System.out.println("网格" + order1 + ":" + key + "  轨迹：" + value);
//                }
//
//            }else{
//                System.out.println("找不到指定的文件");
//            }
//        } catch (Exception e) {
//            System.out.println("读取文件内容出错");
//            e.printStackTrace();
//        }
//    }
//
//    // 网格id列表->获得网格坐标列表
//    public static void gridToXYZ(List<String> tra1GridIDs, List<String> tra2GridIDs, List<Point2D> tra1GridPoint, List<Point2D> tra2GridPoint) {
//        for (int i = 0; i < tra1GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra1GridIDs.get(i)).getMin();
//            tra1GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//        for (int i = 0; i < tra2GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra2GridIDs.get(i)).getMin();
//            tra2GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//    }
//
//    // 计算两点之间的欧氏距离
//    public static double calDistance(Point2D tra1GridPoint, Point2D tra2GridPoint) {
//        return Math.sqrt((tra1GridPoint.getX()-tra2GridPoint.getX())*(tra1GridPoint.getX()-tra2GridPoint.getX()) + (tra1GridPoint.getY()-tra2GridPoint.getY())*(tra1GridPoint.getY()-tra2GridPoint.getY()));
//    }
//
//
//
//    public static void main(String[] args) {
//
//        // 数据集文件路径
//        String filePath = "./src/main/java/org/example/wty/similarity/trajectory.txt";
//
//        // cube的最小最大坐标
//        // x y z
//        double[] min_xyz = new double[3];
//        double[] max_xyz = new double[3];
//
////        // 八叉树和四叉树
////        BoundingBox3D root3D = null;
////        OcTree ocTree = null;
////        BoundingBox2D root2D = null;
////        QuadTree quadTree = null;
//
////        // 三个倒排表
////        HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
////        HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
////        HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs
//
//        // 读取数据集文件
//        readTxtFile(filePath, TCList, CTList, GTList, min_xyz, max_xyz);
//
//        // 构造八叉树和四叉树
////        double min_x = min_xyz[0];
////        double min_y = min_xyz[1];
////        double min_z = min_xyz[2];
////        double max_x = max_xyz[0];
////        double max_y = max_xyz[1];
////        double max_z = max_xyz[2];
////        BoundingBox3D root3D = new BoundingBox3D(new Point3D(min_x, min_y, min_z), new Point3D(max_x, max_y, max_z));
////        OcTree ocTree = new OcTree(root3D, maxLevel, "2022-03-11", 10);
////        BoundingBox2D root2D = new BoundingBox2D(new Point2D(min_x, min_y), new Point2D(max_x, max_y));
////        QuadTree quadTree = new QuadTree(root2D, maxLevel, "2022-03-11");
//
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
//
//        // 计算两个grid之间的距离时，统一使用左下方的点，即计算两个网格左下角点的距离
//        // 以下两个list存储轨迹经过的grid的左下角的点
//        List<Point2D> tra1GridPoint = new ArrayList<>();
//        List<Point2D> tra2GridPoint = new ArrayList<>();
//        for (int i = 0; i < tra1GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra1GridIDs.get(i)).getMin();
//            tra1GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//        for (int i = 0; i < tra2GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra2GridIDs.get(i)).getMin();
//            tra2GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//
//        double distance = gridFrechet(tra1GridPoint, tra2GridPoint);
//        System.out.println("Frechet Distance: " +  distance);
//
//    }
//}

package org.example.wty.similarity;

import cn.edu.whu.index.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CubeToGridFrechet {
    static int maxLevel; // 构造八叉树和四叉树的最大层数
    public static List<String> R = new ArrayList<>(); // topk结果集
    static int theta = 1000;
    static int lambda = 50;
    static double length;

    // 三个倒排表
    static HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
    static HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
    static HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs

    // 八叉树和四叉树
    static BoundingBox3D root3D = null;
    static OcTree ocTree = null;
    static BoundingBox2D root2D = null;
    static QuadTree quadTree = null;

    // topk遍历初始轮数
    static final int INITIAL_ROUND_FOR_H_OR_F = 1;


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

                        length = (max_x - min_x) / Math.pow(2, maxLevel);
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
//
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

    // topk
    public static void topkWithFrechetOrHausdorff (String queryTraID, List<String> queryTraGridIDs, int k, List<String> R) {
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

                // 这里计算的和论文里的不一样，直接用的网格之间的距离
                // unseenUB是查询轨迹每个网格扩张时的UB
//                double upperBound = - round * (quadTree.getBoundingBox(queryGridID).getMax().getX() - quadTree.getBoundingBox(queryGridID).getMin().getX()) * Math.sqrt(2);
                double upperBound = - round * length * Math.sqrt(2);
                unseenUpperBounds[i] = upperBound;
                overallUnseenedUpperBound = Math.min(upperBound, overallUnseenedUpperBound);

                // 取出扫描到的grids
                List<String> grids = new ArrayList<>();
                grids = quadTree.expand(queryGridID, round);

                for (String grid : grids) {
                    // 计算两个网格之间的距离
                    Point2D queryGridPoint = quadTree.getBoundingBox(queryGridID).getMin();
                    Point2D currentGridPoint = quadTree.getBoundingBox(grid).getMin();
                    // 注意是负数
                    Double score = - calDistance(queryGridPoint, currentGridPoint);

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
                    String gid = ocTree.toGid(canCubeIDs.get(i), quadTree);
                    if (i > 0 && canGridIDs.get(canGridIDs.size() - 1).equals(gid)) continue;
                    canGridIDs.add(gid);
                }

                if (canGridIDs == null) continue;
                double realDist = 0;

                List<Point2D> queryGridPoint = new ArrayList<>();
                List<Point2D> candidateGridPoint = new ArrayList<>();
                gridToXYZ(queryTraGridIDs, canGridIDs, queryGridPoint, candidateGridPoint);

//                System.out.println(curTrajId);
                realDist = gridFrechet(queryGridPoint, candidateGridPoint);

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

    public static void main(String[] args) {

        // 数据集文件路径
        String filePath = "./src/main/java/org/example/wty/similarity/5.txt";

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


//        // ----------------------------计算两条轨迹的相似度----------------------------------------------------------------
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
//        // cubeid转化为gridid
//        List<String> tra1GridIDs = new ArrayList<>();
//        List<String> tra2GridIDs = new ArrayList<>();
//        for (int i = 0; i < tra1CubeIDs.size(); i++){
//            String gid1 = ocTree.toGid(tra1CubeIDs.get(i), quadTree);
//            System.out.println("gridid1:" + gid1);
//            if (i > 0 && tra1GridIDs.get(tra1GridIDs.size() - 1).equals(gid1)) continue;
//            tra1GridIDs.add(gid1);
//        }
//        for (int i = 0; i < tra2CubeIDs.size(); i++){
//            String gid2 = ocTree.toGid(tra2CubeIDs.get(i), quadTree);
//            System.out.println("gridid2:" + gid2);
//            if (i > 0 && tra2GridIDs.get(tra2GridIDs.size() - 1).equals(gid2)) continue;
//            tra2GridIDs.add(gid2);
//        }
//
//        // 计算两个grid之间的距离时，统一使用左下方的点，即计算两个网格左下角点的距离
//        // 以下两个list存储轨迹经过的grid的左下角的点
//        List<Point2D> tra1GridPoint = new ArrayList<>();
//        List<Point2D> tra2GridPoint = new ArrayList<>();
//        for (int i = 0; i < tra1GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra1GridIDs.get(i)).getMin();
//            tra1GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//        for (int i = 0; i < tra2GridIDs.size(); i++) {
//            Point2D point = quadTree.getBoundingBox(tra2GridIDs.get(i)).getMin();
//            tra2GridPoint.add(point);
//            System.out.println(point.getX() + ", " + point.getY());
//        }
//
//        double distance = gridHausdorff(tra1GridPoint, tra2GridPoint);
//        System.out.println(distance);
//        // ---------------------------------计算完成---------------------------------------------------------------------

        // ----------------------------------输入一条查询轨迹计算它的topk----------------------------------------------------
        // 创建输入对象
        Scanner sc = new Scanner(System.in);

        // 获取用户输入的字符串
        String queryTraID = null;
        System.out.print("请输入轨迹id：");
        queryTraID = sc.nextLine();

//        System.out.println("输入的轨迹id：" + queryTraID);

//         存储轨迹id经过的cubes
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
        topkWithFrechetOrHausdorff(queryTraID, queryTraGridIDs, k, R);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Top-k查询时间：" + (endTime - startTime) + "ms");
        // -------------------------------------------topk查询结束--------------------------------------------------------

    }
}

