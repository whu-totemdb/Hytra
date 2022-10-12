//
// Created by rauchy on 2022/3/7.
//

#ifndef INVERTED_LSM_DISKLEVEL_H
#define INVERTED_LSM_DISKLEVEL_H
#include<vector>
#include<string>
#include<map>
#include"DiskRUN.h"
class DiskLevel {
public:
    DiskLevel(unsigned int level,unsigned int run_nums,unsigned int run_size_threshold,unsigned int run_element_length,const std::vector<std::string>& keys);
    ~DiskLevel();
    void get_run(const std::string& key, DiskRUN* &diskrun);
    DiskRUN* get_run(std::string key);

private:
    unsigned int _run_nums;
    unsigned int _level;
    unsigned int _run_size_threshold;
    unsigned int _run_element_length;
    std::map<std::string,DiskRUN*> _disk_maps;

};


#endif //INVERTED_LSM_DISKLEVEL_H
