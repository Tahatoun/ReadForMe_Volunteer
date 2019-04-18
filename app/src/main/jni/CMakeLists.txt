cmake_minimum_required(VERSION 3.4.1)

set(
	PATH_TO_SUPERPOWERED
	CACHE STRING ""
)

message(${ANDROID_ABI})

file(GLOB CPP_FILES "*.cpp")
file(GLOB C_FILES "*.c")
file(GLOB CLAME_FILES "./libmp3lame/*.c")
add_library(
	wrapper
	SHARED
	${C_FILES}
	${CLAME_FILES}
)

include_directories(src/main/jni)
include_directories(src/main/jni/libmp3lame)
target_include_directories(wrapper PRIVATE
                           src/main/jni/libmp3lame)
target_include_directories(wrapper PRIVATE
                           src/main/jni)

target_link_libraries(
    wrapper
    log
)

