package edu.whu.hyk.exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  java.util.*;



public class Simlarity {

    private static final Logger logger = LoggerFactory.getLogger(Simlarity.class);

    //Longest Overlapping Cubes with no merging
    public static void LOC(int tid, int k, HashMap<String, HashSet<Integer>> CT, HashMap<Integer, List<String>> TC){
        long start = System.nanoTime();
        for (int repeat = 0; repeat < 10; repeat++) {
            HashSet<Integer> canTids = new HashSet<>();
            List<String> cubeSeq = TC.get(tid);
            for (String cid : cubeSeq){
                canTids.addAll(CT.get(cid));
            }
            PriorityQueue<Integer> maxHeap = new PriorityQueue<>((tid1, tid2) ->{
                List<String> cubeSeq1 = TC.get(tid1);
                List<String> cubeSeq2 = TC.get(tid2);
                cubeSeq1.retainAll(cubeSeq);
                cubeSeq2.retainAll(cubeSeq);
                return cubeSeq2.size() - cubeSeq1.size();
            });

            maxHeap.addAll(canTids);
            HashSet<Integer> res = new HashSet<>();
            for (int i = 0; i < k; i++) {
                if(!maxHeap.isEmpty()){
                    res.add(maxHeap.poll());
                }
            }
        }
        long end = System.nanoTime();
        logger.info("[Similarity] LOC ------ "+ (end - start)/1e10);

    }



    // 计算两条轨迹相似度：把cubes转化成对应的grids然后用LORS的思想计算相似度
    public static int gridLORS(List<Integer> tra1GridIDs, List<Integer> tra2GridIDs) {
        if (tra1GridIDs == null || tra2GridIDs == null || tra1GridIDs.size() == 0 || tra2GridIDs.size() == 0) {
            return 0;
        }

        int theta = 1000;

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
    public static void topkWithLORS(HashMap<Integer, List<Integer>> GTList, List<Integer> queryTraGridIDs, int k) {
        long start = System.nanoTime();

            // 1. 找到经过【查询轨迹经过的grid】的所有轨迹作为candidates
            List<Integer> candidates = new ArrayList<>();
            HashMap<Integer, Integer> candidatesWithUpperBound = new HashMap<>();
            HashMap<Integer, List<Integer>> candidatesWithGrids = new HashMap<>();
            int theta = 1000;

            long filter_startTime = System.currentTimeMillis(); //获取开始时间

            // 2. 求出candidates中的每一条轨迹和查询轨迹的相似度上限UB
            // 遍历extendedGrids，获得所有需要扫描的轨迹
            for (int i = 0; i < queryTraGridIDs.size(); i++) {
                int gridid = queryTraGridIDs.get(i);
                List<Integer> tras = GTList.get(gridid);

                if (tras == null) continue;

                // 直接遍历轨迹，加入candidatesWithUB里
                for (int tra : tras) {
                    List<Integer> grids = new ArrayList<>();
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
//        System.out.println("filter耗时：" + (filter_endTime - filter_startTime) + "ms");

            // 删除查询路径本身
//        candidates.remove(queryTraID);
//        candidatesWithGrids.remove(queryTraID);
//        candidatesWithUpperBound.remove(queryTraID);

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
            PriorityQueue<Map.Entry<Integer, Integer>> upperBoundRank = new PriorityQueue<>((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            upperBoundRank.addAll(candidatesWithUpperBound.entrySet());

            // key for trajectory id, value for its exact score            poll从小到大
            PriorityQueue<Map.Entry<Integer, Integer>> topKHeap = new PriorityQueue<>(Map.Entry.comparingByValue());
            double bestKth = -Integer.MAX_VALUE;

            long refine_startTime = System.currentTimeMillis(); //获取开始时间

            // 4. 依次求出candidates和查询轨迹的真实相似度
            // 5. 如果R中的第k条候选轨迹比剩下未计算真实值的candidates的最大UB大，直接返回R
            while (!upperBoundRank.isEmpty()) {
                Map.Entry<Integer, Integer> entry = upperBoundRank.poll();

                if (topKHeap.size() >= k && bestKth > entry.getValue()) break; // early termination

                List<Integer> candidateWithGrids = candidatesWithGrids.get(entry.getKey()); // 本身是有先后顺序的

                int exactValue = gridLORS(queryTraGridIDs, candidateWithGrids);

                entry.setValue(exactValue);
                topKHeap.add(entry);
                if (topKHeap.size() > k) topKHeap.poll();

                bestKth = topKHeap.peek().getValue();
            }

            long refine_endTime = System.currentTimeMillis(); //获取开始时间
//        System.out.println("refine耗时：" + (refine_endTime - refine_startTime) + "ms");
            List<Integer> R = new ArrayList<>();
            while (topKHeap.size() > 0) {
                R.add(topKHeap.poll().getKey());
            }

        long end = System.nanoTime();
        logger.info("[Similarity] LORS ------ "+ (end - start)/1e9);
//        R.forEach(System.out::print);
//        System.out.println(R);
//        return;
    }


}
