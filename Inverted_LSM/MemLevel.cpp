//
// Created by rauchy on 2022/3/11.
//
#include "MemLevel.h"


MemLevel::MemLevel(unsigned int element_length, unsigned int vsize_threshold,std::vector<std::string> keys)
:_element_length(element_length),_vsize_threshold(vsize_threshold)
{
    for(const auto& s:keys){
        _mem_keys.insert(s);
    }
}

MemLevel::~MemLevel() {
    for(auto it:_memruns){
        delete it.second;
    }
}

MemRUN *MemLevel::get_memrun(const std::string& key) {

    // 如果包含给定key，直接返回
    if(_memruns.count(key)==1){
        return _memruns[key];
    }
    // 否则，创建memrun后返回
    else{
        MemRUN* memrun = new MemRUN(_element_length,_vsize_threshold,key);
        _memruns[key] = memrun;
        return memrun;
    }

}

bool MemLevel::contains_key(const std::string& key) {

    return _mem_keys.count(key)==1;
}
