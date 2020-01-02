#!/usr/bin/env bash

_CLASSPATH=.:../jar/*:../deps:../deps/*:./algo/*:./traffic/*

if [ -z ${DERBY_PATH} ];
    then
        echo "Set DERBY_PATH before continuing!";
        exit;
    else
        echo "Using Derby directory '$DERBY_PATH'";
fi

java \
    -Xmx6g \
    -Djava.library.path=../deps \
    -Dderby.language.statementCacheSize=200 \
    -Dderby.locks.deadlockTrace=false \
    -Dderby.locks.monitor=false \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.storage.debug=false \
    -Djargors.controller.debug=true \
    -Djargors.controller.clock_start=0 \
    -Djargors.controller.clock_end=1800 \
    -Djargors.client.debug=false \
    -cp $_CLASSPATH:$DERBY_PATH/derby.jar \
app $@


