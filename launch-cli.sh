#!/usr/bin/env bash
_CLASSPATH=.:jar/*:dep:dep/*
java \
    -Xmx6g \
    -Djava.library.path=dep \
    -Dderby.language.statementCacheSize=200 \
    -Dderby.locks.deadlockTrace=false \
    -Dderby.locks.monitor=false \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.storage.debug=false \
    -Djargors.controller.debug=false \
    -Djargors.client.debug=true \
    -Djargors.traffic.debug=false \
    -cp $_CLASSPATH:$DERBY_PATH/derby.jar \
com.github.jargors.ui.Command $@


