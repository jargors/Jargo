#!/usr/bin/env bash
java -Xmx6g -cp .:$CLASSPATH/*:$DERBYPATH/derby.jar StoragePerformanceTest $@
