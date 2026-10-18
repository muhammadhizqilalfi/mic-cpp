cmake_minimum_required(VERSION 3.22.1)

project("mic_cpp")

add_library(
    mic_cpp
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    mic_cpp
    ${log-lib}
)