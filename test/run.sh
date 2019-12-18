#!/usr/bin/env bash
java \
    -Xmx6g \
    -Djava.library.path=../deps \
    -cp .:../deps:../deps/*:../jar/*:$DERBY_PATH/derby.jar:$JUNIT/* \
org.junit.runner.JUnitCore SchedulesTest
