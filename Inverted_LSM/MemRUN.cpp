//
// Created by rauchy on 2022/3/6.
//

#include "MemRUN.h"

MemRUN::MemRUN(unsigned int element_length,unsigned int v_threshold,std::string k):element_length(element_length),key(k),vsize_threshold(v_threshold){
    vlist = std::vector<std::string>();

}
MemRUN::~MemRUN(){}
void MemRUN::add_v(std::string v) {
    vlist.push_back(v);

}
void MemRUN::delete_v(std::string v){
    for(auto it=vlist.begin();it!=vlist.end();){
        if(*it==v){
            it = vlist.erase(it);
        }
        else{
            it++;
        }
    }
}

bool MemRUN::is_full(){
    return vlist.size()>=vsize_threshold;
}

std::vector<std::string> MemRUN::get_all_elements() {
    return vlist;
}

std::string MemRUN::get_key() {
    return key;
}

unsigned int MemRUN::get_element_length() {
    return element_length;
}

void MemRUN::clear() {
    vlist.clear();
}
