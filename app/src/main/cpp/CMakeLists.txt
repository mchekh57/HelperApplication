cmake_minimum_required(VERSION 3.4.1)

add_library(process_helper_lib SHARED process_helper_lib.cpp)

find_library(c_lib c)
find_library(log-lib log)

set(CMAKE_CXX_STANDARD 11)

target_link_libraries(process_helper_lib ${c_lib} ${log_lib} ${CMAKE_CXX_STANDARD_LIBRARIES})
