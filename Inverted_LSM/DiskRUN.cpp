//
// Created by rauchy on 2022/3/6.
//

#include "DiskRUN.h"
#include "FileMapper.h"
DiskRUN::DiskRUN(unsigned int element_length,std::string key,unsigned int level,unsigned int vsize_threshold)
:_element_length(element_length),_key(key),_level(level),_vsize_threshold(vsize_threshold)
{
    _fmapper = new FileMapper(_key+ std::to_string(level),_vsize_threshold,_element_length);
    _cur_size = 0;
}

DiskRUN::~DiskRUN(){
    delete _fmapper;
}

std::vector<std::string> DiskRUN::get_all_elements(){
    return _fmapper->read_all_element();

}


bool DiskRUN::isfull(){
    return _cur_size>=_vsize_threshold;
}

// 在当前大小的基础上将容量和阈值同时 *2
void DiskRUN::expand(){
    _fmapper->expand_size();
    _vsize_threshold =_cur_size*2;
}

// 单次写入效率低，弃用
void DiskRUN::add_element(std::string s){
    _fmapper->write_element(s);
    _cur_size++;
}

void DiskRUN::clear(){
    _fmapper->clear();
    _cur_size=0;
}

std::string DiskRUN::get_key() {
    return _key;
}

unsigned int DiskRUN::get_element_length() {
    return _element_length;
}

void DiskRUN::adjust_size(unsigned int new_size) {
    _fmapper->adjust_size(new_size);

}

unsigned int DiskRUN::get_vsize_threshold() {
    return _vsize_threshold;
}

void DiskRUN::add_batch(std::set<std::string> s) {
    _cur_size+=s.size();
    _fmapper->write_batch(std::move(s));

}

bool DiskRUN::is_empty() {
    return _cur_size==0;
}

unsigned int DiskRUN::get_level() {
    return _level;
}
