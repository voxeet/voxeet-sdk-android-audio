#
# Copyright 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -O3 -DANDROID_ARM_NEON=true")

include(FindOpenMP)


if(OPENMP_FOUND)
  set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
else(OPENMP_FOUND)
  message("ERROR: OpenMP could not be found.")
endif(OPENMP_FOUND)

### INCLUDE OBOE LIBRARY ###

# Set the path to the Oboe library directory

set (OBOE_DIR ../oboe)

# Add the Oboe library as a subproject. Since Oboe is an out-of-tree source library we must also
# specify a binary directory
add_subdirectory(${OBOE_DIR} ./oboe-bin)

# Include the Oboe headers
include_directories(${OBOE_DIR}/include)

# Debug utilities
set (DEBUG_UTILS_PATH "../hello-oboe-debug-utils")
set (DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})

# App specific sources
set (APP_DIR src/main/cpp)
file (GLOB_RECURSE APP_SOURCES
    ${APP_DIR}/jni_bridge.cpp
    ${APP_DIR}/PlayAudioEngine.cpp
    ${APP_DIR}/SineGenerator.cpp
)


# Build the libhello-oboe library
add_library(hello-oboe SHARED
            ${DEBUG_UTILS_SOURCES}
            ${APP_SOURCES}
            )

# Specify the libraries needed for hello-oboe
target_link_libraries(# Specifies the target library
                      hello-oboe
                      # Links the log library to the target library
                      android
                      log
                      oboe)
target_compile_definitions(hello-oboe PRIVATE VXT_LIB=1)