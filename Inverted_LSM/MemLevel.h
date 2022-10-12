//
// Created by rauchy on 2022/3/11.
//

#ifndef INVERTED_LSM_MEMLEVEL_H
#define INVERTED_LSM_MEMLEVEL_H
#include "MemRUN.h"
#include<map>
class MemLevel{
public :
    MemLevel(unsigned int element_length,unsigned int vsize_threshold,std::vector<std::string> keys);
    ~MemLevel();
    MemRUN* get_memrun(const std::string& key);
    bool contains_key(const std::string& key);
private:
    unsigned int _element_length;
    unsigned int _vsize_threshold;
    std::map<std::string,MemRUN*> _memruns;
    std::set<std::string> _mem_keys;
};
#endif //INVERTED_LSM_MEMLEVEL_H
