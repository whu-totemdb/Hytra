package org.example.wty.topk;

import java.util.List;

// 算了 直接去CubeToGridLORS里面写topk吧
public class TopkWithLORS {

    public static  List<String> R; // 结果集
    public static void topk (List<String> queryTraGridIDs, int k, List<String> R) {
        // 1. 找到经过【查询轨迹经过的grid】的所有轨迹作为candidates

        // 2. 求出candidates中的每一条轨迹和查询轨迹的相似度上限UB
        // 3. 根据UB从大到小给candidates排序
        // 4. 依次求出candidates和查询轨迹的真实相似度并更新结果集R
        // 5. 如果R中的第k条候选轨迹比剩下未计算真实值的candidates的最大UB大，直接返回R
    }
}
