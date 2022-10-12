//
// Created by rauchy on 2022/3/6.
//

// 针对倒排索引的LSM tree, k,v均用string表示
#ifndef INVERTED_LSM_MEMRUN_H
#define INVERTED_LSM_MEMRUN_H
#include<map>
#include<set>
#include<vector>

class MemRUN {
public:
    MemRUN(unsigned int element_length,unsigned int v_threshold,std::string k);
    ~MemRUN();
    void add_v(std::string v);
    void delete_v(std::string v);
    bool is_full();
    std::vector<std::string> get_all_elements();
    std::string get_key();
    unsigned int get_element_length();
    void clear();
private:
    unsigned int element_length;
    std::string key;
    std::vector<std::string> vlist;
    unsigned int vsize_threshold;
};


#endif //INVERTED_LSM_MEMRUN_H
