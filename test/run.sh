#!/usr/bin/env bash
java -cp .:$CLASSPATH/*:$DERBYPATH/derby.jar StorageTest $@
