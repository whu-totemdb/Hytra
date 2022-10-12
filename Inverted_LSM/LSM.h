//
// Created by rauchy on 2022/3/6.
//

#ifndef INVERTED_LSM_LSM_H
#define INVERTED_LSM_LSM_H
#include<vector>
#include "MemRUN.h"
#include "DiskLevel.h"
#include <map>
#include <set>
#include "MemLevel.h"
class LSM {
    public:
        LSM(std::map<std::string, std::string> merge_maps,
            std::vector<std::vector<std::string>> keys_per_level,
            std::vector<unsigned int> element_size_threshold_per_level,
            std::vector<unsigned int> element_length_per_level,
            std::vector<unsigned int> runs_per_level);

        ~LSM();

        void merge_memruns_to_level(MemRUN *cur_run, DiskRUN *to_merge_run);

        void merge_next_level(DiskRUN *cur_run, DiskRUN *to_merge_run, unsigned int next_level);

        std::set<std::string> get_items_for_key(std::string key);

        void insert_kv(const std::string& k, std::string v);


        // 暂时不考虑删除
        void delete_kv(std::string k, std::string v);


    private:

        std::vector<DiskLevel *> _disk_levels;
        MemLevel* _mem_level;
        std::map<std::string, std::string> _merge_maps;  // 记录上一层中的run与下一层哪一个run合并
        std::vector<std::vector<std::string>> _keys_per_level;   // 记录每一层的run对应的key值
        std::vector<unsigned int> _element_size_threshold_per_level;  // 记录每一层的run对应的元素数量阈值
        std::vector<unsigned int> _element_length_per_level;   // 记录每一层的元素长度
        std::vector<unsigned int> _runs_per_level;   //  记录每一层run的数量
        // 所有的run的element_size设为相同
        std::map<std::string, MemRUN *> _memruns_for_key; //记录每个key对应的 MemRUN，插入k,v时用

        // 不考虑删除功能

};


#endif //INVERTED_LSM_LSM_H
