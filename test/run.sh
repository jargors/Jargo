#!/usr/bin/env bash
java -Xmx4g -cp .:../jargors-storage-1.0.0.jar:$DERBYPATH/derby.jar StorageTest $@
