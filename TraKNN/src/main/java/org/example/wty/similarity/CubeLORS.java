package org.example.wty.similarity;

import cn.edu.whu.index.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CubeLORS {

    static int maxLevel; // 构造八叉树和四叉树的最大层数
    public static List<String> R = new ArrayList<>(); // topk结果集
    static int theta = 1000;

    // 三个倒排表
    static HashMap<String, List> TCList = new HashMap<String, List>(); // trajectoryID-cubeIDs
    static HashMap<String, List> CTList = new HashMap<String, List>(); // cubeID-trajectoryIDs
//    static HashMap<String, List> GTList = new HashMap<String, List>(); // gridID-trajectoryIDs

    // 八叉树和四叉树
    static BoundingBox3D root3D = null;
    static OcTree ocTree = null;
    static BoundingBox2D root2D = null;
    static QuadTree quadTree = null;

    // 1. 构建八叉树和四叉树
    // 2. 构建CTList、TCList
    public static void readTxtFile(String filePath, HashMap<String, List> TCList,  HashMap<String, List> CTList, double[] min_xyz, double[] max_xyz){
        try {
//            long startTime = System.currentTimeMillis(); //获取开始时间
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

                        }

                    }
                }
                read.close();
//                long endTime = System.currentTimeMillis(); //获取结束时间
//                System.out.println("程序运行时间：" + (endTime - startTime) + "ms");

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

    public static void cubeTopkWithLORS (String queryTraID, List<String> queryTraCubeIDs, int k, List<String> R) {
        // 1. 找到经过【查询轨迹经过的cube】的所有轨迹作为candidates
        List<String> candidates = new ArrayList<>();
        HashMap<String, Integer> candidatesWithSmilarity= new HashMap<>();

        int size = queryTraCubeIDs.size();
        for (int i = 0; i < size; i++) {
            String cubeid = queryTraCubeIDs.get(i);
            List<String> tras = CTList.get(cubeid); // 取出经过该cube的轨迹id
            tras.remove(queryTraID);

            for (String tra : tras) {
                if (candidatesWithSmilarity.containsKey(tra)) continue;

                List<String> traCubes = TCList.get(tra);

                List<String> queryTemp = new ArrayList<>(queryTraCubeIDs);
                List<String> canTemp = new ArrayList<>(traCubes);
                int querySize = queryTemp.size();
                queryTemp.removeAll(canTemp);
                // 求出querytra和cantra的相似度
                int similarity = querySize - queryTemp.size();

                candidatesWithSmilarity.put(tra, similarity);
            }
        }

        // poll从大到小
        PriorityQueue<Map.Entry<String, Integer>> similarityRank = new PriorityQueue<>((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        similarityRank.addAll(candidatesWithSmilarity.entrySet());

        for (int i = 1; i <= k; i++) {
            R.add(similarityRank.poll().getKey());
        }
    }

    public static void main(String[] args) {
        // 数据集文件路径
        String filePath = "./src/main/java/org/example/wty/similarity/trajectories1.txt";

        // cube的最小最大坐标
        // x y z
        double[] min_xyz = new double[3];
        double[] max_xyz = new double[3];

        // 读取数据集文件
        readTxtFile(filePath, TCList, CTList, min_xyz, max_xyz);


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

        // topk的k
        int k = 50;
        // 进行topk查询
        long startTime = System.currentTimeMillis(); //获取开始时间
        cubeTopkWithLORS(queryTraID, queryTraCubeIDs, k, R);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("Top-k查询时间：" + (endTime - startTime) + "ms");
        System.out.println(R);

        // -------------------------------------------topk查询结束--------------------------------------------------------

    }
}
