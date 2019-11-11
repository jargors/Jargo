#!/usr/bin/env bash
java \
    --module-path $CLASSPATH \
    --add-modules javafx.controls,javafx.fxml \
    -Xmx6g \
    -Djava.library.path=$LD_LIBRARY_PATH \
    -Dderby.storage.pageCacheSize=8000 \
    -cp .:$CLASSPATH/*:jar/*:$DERBYPATH/derby.jar \
com.github.jargors.Desktop
