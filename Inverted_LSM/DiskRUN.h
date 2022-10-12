//
// Created by rauchy on 2022/3/6.
//

#ifndef INVERTED_LSM_DISKRUN_H
#define INVERTED_LSM_DISKRUN_H
#include<string>
#include "FileMapper.h"
#include<vector>
class DiskRUN {
public:
    DiskRUN(unsigned int element_length,std::string key,unsigned int level,unsigned int vsize_threshold);
    ~DiskRUN();
    std::vector<std::string> get_all_elements();
    bool isfull();
    void expand();
    void add_element(std::string s);
    void clear();
    std::string get_key();
    unsigned int get_element_length();
    void adjust_size(unsigned int new_size);
    unsigned int get_vsize_threshold();
    void add_batch(std::set<std::string> s);
    bool is_empty();
    unsigned int get_level();
private:
    unsigned int _element_length;
    std::string _key;
    unsigned int _level;
    unsigned int _vsize_threshold;
    unsigned int _cur_size;
    FileMapper* _fmapper;

};


#endif //INVERTED_LSM_DISKRUN_H
