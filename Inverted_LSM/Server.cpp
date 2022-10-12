//
// Created by rauchy on 2022/3/11.
//

#include "Server.h"
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <cstring>
Server::Server(LSM* l):_l(l) {

}

Server::~Server() {
    shutdown(_listenfd,SHUT_RDWR);
    delete _l;
}

void Server::start() {
    int conn;
    char clientIP[INET_ADDRSTRLEN] = "";
    struct sockaddr_in clientAddr;
    std::string res_ok = "Insert OK!\n";
    std::string res_err = "Insert error\n";
    socklen_t clientAddrLen = sizeof(clientAddr);
    while(true){
        conn = accept(_listenfd, (struct sockaddr*)&clientAddr, &clientAddrLen);
        if(conn<0){
            perror("Accept error!\n");
        }
        inet_ntop(AF_INET, &clientAddr.sin_addr, clientIP, INET_ADDRSTRLEN);
        printf("Connect from %s\n",clientIP);
        char buf[255];
        while(true){
            memset(buf, 0, sizeof(buf));
            int len = recv(conn, buf, sizeof(buf), 0);
            buf[len] = '\0';
            // 断开连接
            if (strncasecmp(buf, "exit",4) == 0) {
                printf("Disonnect from %s\n",clientIP);
                break;
            }
            // 添加kv对
            else if (strncasecmp(buf,"put",3)==0){
                std::string tmp = buf;
                unsigned int pos1 = tmp.find(":");
                unsigned int pos2 = tmp.find(",");
                std::string key = tmp.substr(pos1+1,pos2-pos1-1);
                std::string value = tmp.substr(pos2+1,len-pos2);
                printf("Insert %s : %s\n", key.c_str(),value.c_str());
                _l->insert_kv(key,value);
                send(conn,res_ok.c_str(),res_ok.length(),0);
            }
            // 根据k查找
            else if (strncasecmp(buf,"get",3)==0){
                std::string tmp = buf;
                unsigned int pos = tmp.find(":");
                std::string key = tmp.substr(pos+1,len-pos);
                auto res = _l->get_items_for_key(key);
                std::string res_str;
                for(auto s:res){
                    res_str.append(s);
                }
                res_str.append("\n");
                send(conn,res_str.c_str(),res_str.length(),0);
                printf("Send result with length: %d\n",res_str.length());
            }
            // 关闭程序
            else if(strncasecmp(buf,"std",3)==0){
                printf("Close socket\n");
                return;
            }
            // 打印错误信息
            else {
                send(conn,res_err.c_str(),res_err.length(),0);
                printf("Error input : %s \n",buf);
            }

        }

    }
}

void Server::init() {
    _listenfd = socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(9200);
    addr.sin_addr.s_addr = INADDR_ANY;

    if(bind(_listenfd, (struct sockaddr*)&addr, sizeof(addr))==-1){
        perror("Bind error!\n");
        return;
    }

    if(listen(_listenfd,5)==-1){
        perror("Listen error!\n");
        return;
    }

}
