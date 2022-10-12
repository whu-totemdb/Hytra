//
// Created by rauchy on 2022/3/13.
//
#include <iostream>
#include"LSM.h"
#include <sys/socket.h>
#include <fstream>
#include <time.h>
extern LSM* load_lsm_config();

void test_insert_kv(LSM* l);
int main() {

    LSM* l = load_lsm_config();
    test_insert_kv(l);
    delete l;
    return 0;

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
    printf("Total time for %d insert operations : %f seconds\n",count,(double)(end-start)/CLOCKS_PER_SEC);

}



