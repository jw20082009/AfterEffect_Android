# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
#ffmpeg的库文件所在位置
set(lib_DIR ../../../../src/main/jniLibs/armeabi-v7a)

#include目录，也就是头文件目录
include_directories(
        src/main/jniLibs/include
        src/main/cpp/include
)

#导入so文件

add_library(libffmpeg SHARED IMPORTED)
set_target_properties(libffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${lib_DIR}/libffmpeg.so)

file(GLOB clip_srcs src/main/cpp/*.cpp)
add_library(native-lib SHARED
        ${clip_srcs}
        )

#支持-std=gnu++11
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

#判断编译器类型,如果是gcc编译器,则在编译选项中加入c++11支持
#if(CMAKE_COMPILER_IS_GNUCXX)
#   set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS}")
#    message(STATUS "optional:-std=c++11")
#endif(CMAKE_COMPILER_IS_GNUCXX)

find_library(ndk-lib
        log
        )

target_link_libraries(native-lib
        libffmpeg
        android
        ${ndk-lib}
        )