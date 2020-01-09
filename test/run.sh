#!/usr/bin/env bash
java \
    -Xmx6g \
    -Djava.library.path=../deps \
    -Dderby.language.logStatementText=true \
    -cp .:algo/*:../deps:../deps/*:../jar/*:$DERBY_PATH/derby.jar:$JUNIT/* \
org.junit.runner.JUnitCore \
    DBUpdateServerServiceT1 \
    DBUpdateServerServiceT2 \
    DBUpdateServerServiceT3
#    DBUpdateServerServiceT3 \
#    CacheT1
#org.junit.runner.JUnitCore \
#    CacheT2
#org.junit.runner.JUnitCore \
#    CacheT1
