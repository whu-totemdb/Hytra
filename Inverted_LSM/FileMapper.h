//
// Created by rauchy on 2022/3/6.
//

#ifndef INVERTED_LSM_FILEMAPPER_H
#define INVERTED_LSM_FILEMAPPER_H
#include<string>
#include<vector>
#include<set>
class FileMapper {

public:
    FileMapper(std::string file_name,int element_size_threshold,int element_length);
    ~FileMapper();
    std::string read_element(unsigned int ele_index);
    void write_element(std::string element);
    void write_batch(std::set<std::string> values);
    void clear();
    std::vector<std::string> read_all_element();
    void expand_size(); //对文件进行扩容操作，将内存文件映射的大小*2
    void adjust_size(unsigned int new_size);  //调整文件大小

    void do_map();    // 打开文件进行写入或读取
    void un_map();    // 关闭文件并同步文件内容

    void open_file();

    void close_file() ;

private:
    int _fd;
    std::string _file_name;
    unsigned int _element_size_threshold;
    unsigned int _element_length;
    unsigned int _cur_size;
    char* _fmap;
    size_t file_size;

};


#endif //INVERTED_LSM_FILEMAPPER_H
