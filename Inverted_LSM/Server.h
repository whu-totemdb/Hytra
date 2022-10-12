//
// Created by rauchy on 2022/3/11.
//

#ifndef INVERTED_LSM_SERVER_H
#define INVERTED_LSM_SERVER_H

#include "LSM.h"
class Server {
public :
    Server(LSM* l);
    ~Server();
    void start();
    void init();
private:
    LSM* _l;
    int _listenfd;

};


#endif //INVERTED_LSM_SERVER_H
