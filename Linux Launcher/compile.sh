#!/bin/sh
clang++ -I "/usr/lib/jvm/java-11-openjdk-amd64/include" -I "/usr/lib/jvm/java-11-openjdk-amd64/include/linux" main.cpp -o SpwnerIDE -ldl
./SpwnerIDE
