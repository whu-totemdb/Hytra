cmake_minimum_required(VERSION 2.8.12.2)
project(Inverted_LSM)

set(CMAKE_CXX_STANDARD 14)

add_executable(Inverted_LSM
        main.cpp
        LSM.cpp
        DiskLevel.cpp
        DiskRUN.cpp
        FileMapper.cpp
        MemRUN.cpp
        MemLevel.cpp
        Server.cpp
        LSMUtils.cpp
        )

add_executable(test_insert_operation
        test_insert_operation.cpp
        LSM.cpp
        DiskLevel.cpp
        DiskRUN.cpp
        FileMapper.cpp
        MemRUN.cpp
        MemLevel.cpp
        LSMUtils.cpp

)