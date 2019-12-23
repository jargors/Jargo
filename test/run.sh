#!/usr/bin/env bash
java \
    -Xmx6g \
    -Djava.library.path=../deps \
    -Dderby.language.logStatementText=true \
    -cp .:../deps:../deps/*:../jar/*:$DERBY_PATH/derby.jar:$JUNIT/* \
org.junit.runner.JUnitCore \
    DBUpdateServerServiceT1 \
    DBUpdateServerServiceT2
