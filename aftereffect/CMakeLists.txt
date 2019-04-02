# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
#ffmpeg的库文件所在位置
set(lib_DIR ../../../../libs/armeabi-v7a)
#fdk-aac根目录
set(fdk_aac_dir libs/fdk-aac-0.1.5)

#include目录，也就是头文件目录
include_directories(
        libs/include
        src/main/cpp/include
        src/main/cpp
        ${fdk_aac_dir}/libAACdec/include
        ${fdk_aac_dir}/libAACenc/include
        ${fdk_aac_dir}/libPCMutils/include
        ${fdk_aac_dir}/libFDK/include
        ${fdk_aac_dir}/libSYS/include
        ${fdk_aac_dir}/libMpegTPDec/include
        ${fdk_aac_dir}/libMpegTPEnc/include
        ${fdk_aac_dir}/libSBRdec/include
        ${fdk_aac_dir}/libSBRenc/include
)

#导入so文件

add_library(libffmpeg SHARED IMPORTED)
set_target_properties(libffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${lib_DIR}/libffmpeg.so)

add_library(libFraunhoferAAC SHARED IMPORTED)
set_target_properties(libFraunhoferAAC
        PROPERTIES IMPORTED_LOCATION
        ${lib_DIR}/libFraunhoferAAC.so)

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
        libFraunhoferAAC
        android
        ${ndk-lib}
        )