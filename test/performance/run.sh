#!/usr/bin/env bash
java \
    -Xmx6g \
    -Dderby.language.statementCacheSize=200 \
    -Dderby.storage.pageCacheSize=8000 \
    -cp .:$CLASSPATH/*:$DERBYPATH/derby.jar \
StoragePerformanceTest $@
