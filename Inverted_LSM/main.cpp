#include <iostream>
#include"LSM.h"
#include <sys/socket.h>
#include "Server.h"
#include <fstream>
#include <time.h>
extern LSM* load_lsm_config();

void test_lsm();
void test_load_config();
void test_insert_kv(LSM* l);
int main() {

    LSM* l = load_lsm_config();
    /*Server s(l);
    s.init();
    s.start();*/
    test_insert_kv(l);
    delete l;
    return 0;

}

void test_lsm(){
    std::map<std::string,std::string> merge_map;
    std::vector<std::vector<std::string>> keys_per_level;
    std::vector<unsigned int> element_size_threshold_per_level={4,5,6,7};
    std::vector<unsigned int> element_length_per_level={4,4,4,4};
    std::vector<unsigned int> runs_per_level={4,3,2,1};
    std::vector<std::string> keys_run1={"a","b","c","d"};
    std::vector<std::string> keys_run2={"e","f","g"};
    std::vector<std::string> keys_run3={"h","i"};
    std::vector<std::string> keys_run4={"k"};
    keys_per_level.push_back(keys_run1);
    keys_per_level.push_back(keys_run2);
    keys_per_level.push_back(keys_run3);
    keys_per_level.push_back(keys_run4);
    merge_map["a"] = "e";
    merge_map["b"] = "e";
    merge_map["c"] = "f";
    merge_map["d"] = "g";
    merge_map["e"] = "h";
    merge_map["f"] = "h";
    merge_map["g"] = "i";
    merge_map["h"] = "k";
    merge_map["i"] = "k";

    LSM l(merge_map,keys_per_level,element_size_threshold_per_level,element_length_per_level,runs_per_level);

    l.insert_kv("a","0000");
    l.insert_kv("a","0003");
    l.insert_kv("a","0004");
    l.insert_kv("a","0005");
    l.insert_kv("a","0006");
    std::set<std::string> s1 = l.get_items_for_key("a");
    l.insert_kv("a","0007");
    l.insert_kv("a","0008");
    l.insert_kv("a","0009");
    std::set<std::string> s2 = l.get_items_for_key("a");
    l.insert_kv("b","0001");
    l.insert_kv("c","0002");
}

void test_load_config(){
    LSM* l = load_lsm_config();
}

void test_insert_kv(LSM* l){
    std::ifstream put_file("./operation/put.config");
    std::string line;
    auto start = clock();
    int count = 0;
    if(put_file){
        while(getline(put_file,line)){
            unsigned int pos1 = line.find(":");
            unsigned int pos2 = line.find(",");
            std::string key = line.substr(pos1+1,pos2-pos1-1);
            std::string value = line.substr(pos2+1,line.length()-pos2);
            l->insert_kv(key,value);
            count++;
        }
    }
    auto end =clock();
    printf("Total time for %d insert operations : %ld",count,(end-start)/CLOCKS_PER_SEC);

}


