#!/usr/bin/env bash
J_LIBPATH=deps/*:deps/commons-dbcp2-2.7.0/*:deps/commons-pool2-2.7.0/*:deps/commons-logging-1.2/*:deps/javafx-sdk-13.0.1/lib/*
java \
    --module-path deps/javafx-sdk-13.0.1/lib/ \
    --add-modules javafx.controls,javafx.fxml \
    -Xmx6g \
    -Djava.library.path=deps \
    -Dderby.storage.pageCacheSize=8000 \
    -cp .:$J_LIBPATH:jar/*:$DERBY_PATH/derby.jar \
com.github.jargors.desktop.Desktop
