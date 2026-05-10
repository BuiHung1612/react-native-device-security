cmake_minimum_required(VERSION 3.22.1)
project("device-security")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Define source files
set(SOURCE_FILES
    device-security.cpp
)

# Build shared library
add_library(device-security SHARED ${SOURCE_FILES})

# Find required libraries
find_library(log-lib log)

# Link libraries
target_link_libraries(device-security
    ${log-lib}
)

# Compiler flags for security and obfuscation
target_compile_options(device-security PRIVATE
    -fvisibility=hidden
    -fdata-sections
    -ffunction-sections
    -Wall
    -Wextra
    -O2
)

# Linker flags
target_link_options(device-security PRIVATE
    -Wl,--gc-sections
    -Wl,-s
)

# Strip symbols in release
if(CMAKE_BUILD_TYPE MATCHES Release)
    add_custom_command(TARGET device-security POST_BUILD
        COMMAND ${CMAKE_STRIP} --strip-unneeded $<TARGET_FILE:device-security>
    )
endif()
