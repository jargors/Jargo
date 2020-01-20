#!/usr/bin/env bash
java \
    --module-path dep \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    -Xmx6g \
    -Djava.library.path=dep \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.controller.debug=true \
    -Djargors.desktop.debug=false \
    -cp .:dep:dep/*:jar/*:$DERBY_PATH/derby.jar \
com.github.jargors.desktop.Desktop
