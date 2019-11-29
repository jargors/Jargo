#!/usr/bin/env bash
java \
    --module-path deps \
    --add-modules javafx.controls,javafx.fxml \
    -Xmx6g \
    -Djava.library.path=deps \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.controller.debug=true \
    -Djargors.desktop.debug=true \
    -cp .:deps:deps/*:jar/*:$DERBY_PATH/derby.jar \
com.github.jargors.desktop.Desktop
