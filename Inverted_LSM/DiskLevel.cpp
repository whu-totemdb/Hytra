//
// Created by rauchy on 2022/3/7.
//

#include "DiskLevel.h"
#include "DiskRUN.h"
DiskLevel::DiskLevel(unsigned int level,unsigned int run_nums,unsigned int run_size_threshold,unsigned int run_element_length,const std::vector<std::string>& keys)
:_run_nums(run_nums),_run_size_threshold(run_size_threshold),_run_element_length(run_element_length),_level(level)
{

    if(keys.size()!=run_nums){
        perror("Run number not match with their keys");
    }
    // 并不直接创建diskrun,而是等到真正写入文件时再按需创建diskrun,
    // 避免空run占用资源

    /*for(unsigned int i=0;i<_run_nums;i++){
        DiskRUN * run = new DiskRUN(_run_element_length,keys[i],_level,_run_size_threshold);
        _disk_maps.insert(std::pair<std::string,DiskRUN*>(keys[i],run));
    }*/

}


DiskLevel::~DiskLevel() {
    for(auto it = _disk_maps.begin();it!=_disk_maps.end();it++){
        delete it->second;
    }

}


void DiskLevel::get_run(const std::string& key, DiskRUN* &diskrun){
    // 如果存在当前key，则返回其对应的diskrun
    if(_disk_maps.count(key)==1){
        diskrun = _disk_maps[key];
    }
    // 否则，创建新的diskrun并添加到当前disklevel中
    else{
        diskrun = new DiskRUN(_run_element_length,key,_level,_run_size_threshold);
        _disk_maps[key] = diskrun;
    }

}

DiskRUN *DiskLevel::get_run(std::string key) {
    // 存在则直接返回
    if(_disk_maps.count(key)==1){
        return _disk_maps[key];
    }
    // 否则，创建相应的diskrun并返回
    else{
        DiskRUN* diskrun = new DiskRUN(_run_element_length,key,_level,_run_size_threshold);
        _disk_maps[key] = diskrun;
        return diskrun;
    }

}




