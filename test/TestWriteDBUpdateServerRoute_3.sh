#!/usr/bin/env bash
java \
    -Djava.library.path=../deps \
    -cp .:../deps:../deps/*:../jar/*:$DERBY_PATH/derby.jar \
TestWriteDBUpdateServerRoute_3
