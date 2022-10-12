//
// Created by rauchy on 2022/3/6.
//

#include "FileMapper.h"
#include<sys/mman.h>
#include <fcntl.h>
#include <sys/types.h>
#include<cstring>
#include <stdio.h>
#include <unistd.h>


FileMapper::FileMapper(std::string file_name,int element_size_threshold,int element_length)
:_file_name(file_name),_element_size_threshold(element_size_threshold),_element_length(element_length) {

    file_size = element_length * element_size_threshold;
    _cur_size = 0;
    open_file();
    ftruncate(_fd,file_size);
    close_file();
}

FileMapper::~FileMapper(){

    remove(("index_file/"+_file_name).c_str());

}

// 多次单个元素读取效率低，弃用
std::string FileMapper::read_element(unsigned int ele_index){

    open_file();
    do_map();
    size_t offset = ele_index*_element_length;
    char* val = new char[_element_length];
    memcpy(val,_fmap+offset,_element_length);
    std::string s = val;
    delete[] val;
    un_map();
    close_file();
    return s;

}


// 多次单个元素写入效率较低，弃用
void FileMapper::write_element(std::string element){
    open_file();
    do_map();
    size_t offset = _cur_size*_element_length;
    const char* ele = element.c_str();
    memcpy(_fmap+offset,ele,_element_length);
    _cur_size++;
    un_map();
    close_file();
}

// 删除文件并将文件内的元素数量置为0
void FileMapper::clear(){
    remove(("index_file/"+_file_name).c_str());
    _cur_size = 0;
}


// 按批次读取文件，加快读取速度
std::vector<std::string> FileMapper::read_all_element(){
    std::vector<std::string> s;
    open_file();
    ftruncate(_fd,file_size);
    do_map();
    size_t offset=0;
    char* val = new char[_element_length];
    for(unsigned int i=0;i<_cur_size;i++){
        offset = i*_element_length;
        memcpy(val,_fmap+offset,_element_length);
        std::string tmp = val;
        s.push_back(tmp.substr(0,_element_length));
    }
    delete[] val;
    un_map();
    close_file();
    return s;
}

void FileMapper::expand_size(){
    open_file();

    file_size = _cur_size*_element_length*2;
    ftruncate(_fd,file_size);
    close_file();
    _element_size_threshold = _cur_size*2;
}


void FileMapper::adjust_size(unsigned int new_size) {
    open_file();
    file_size = new_size;
    ftruncate(_fd,file_size);
    close_file();

}

void FileMapper::do_map() {
    _fmap = (char*) mmap(0,file_size,PROT_READ | PROT_WRITE, MAP_SHARED, _fd, 0);
}

void FileMapper::un_map() {
    fsync(_fd);
    if(munmap(_fmap,file_size)==-1){
        perror("unmap error\n");
    }
}

void FileMapper::open_file() {
    _fd = open(("index_file/"+_file_name).c_str(),O_RDWR | O_CREAT ,(mode_t) 0600);
}

void FileMapper::close_file() {
    close(_fd);
}

// 按批次进行写入，提高写入效率
void FileMapper::write_batch(std::set<std::string> values) {
    open_file();
    ftruncate(_fd,file_size);
    do_map();
    size_t offset;
    for(const auto& s:values){
        offset = _cur_size * _element_length;
        const char* tmp = s.c_str();
        memcpy(_fmap+offset,tmp,_element_length);
        _cur_size++;
    }
    un_map();
    close_file();
}
