#!/usr/bin/env bash
java \
    --module-path deps \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    -Xmx6g \
    -Djava.library.path=deps \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.controller.debug=false \
    -Djargors.desktop.debug=false \
    -cp .:deps:deps/*:jar/*:$DERBY_PATH/derby.jar \
com.github.jargors.desktop.Desktop
