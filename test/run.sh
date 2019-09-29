#!/usr/bin/env bash
java -cp .:$JARGO_LIB/*:$DERBYPATH/derby.jar StorageInterfaceTest $@
