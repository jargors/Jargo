#!/usr/bin/env bash
java \
    --module-path dep \
    --add-modules javafx.controls,javafx.fxml,javafx.swing \
    -Xmx6g \
    -Djava.library.path=dep \
    -Dderby.storage.pageCacheSize=8000 \
    -Djargors.controller.debug=false \
    -Djargors.desktop.debug=false \
    -cp .:dep:dep/*:jar/*:$DERBY_HOME/lib/derby.jar \
com.github.jargors.ui.Desktop

