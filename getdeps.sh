#!/usr/bin/env bash
OPENJFX=openjfx-13.0.1_linux-x64_bin-sdk.zip
DBCP2=commons-dbcp2-2.7.0-bin.tar.gz
LOGGING=commons-logging-1.2-bin.tar.gz
POOL2=commons-pool2-2.7.0-bin.tar.gz

mkdir deps
curl https://download2.gluonhq.com/openjfx/13.0.1/$OPENJFX --output deps/$OPENJFX
curl http://mirror.navercorp.com/apache//commons/dbcp/binaries/$DBCP2 --output deps/$DBCP2
curl http://mirror.navercorp.com/apache//commons/logging/binaries/$LOGGING --output deps/$LOGGING
curl http://apache.mirror.cdnetworks.com//commons/pool/binaries/$POOL2 --output deps/$POOL2

unzip deps/$OPENJFX -d deps/
tar xf deps/$DBCP2 -C deps
tar xf deps/$LOGGING -C deps
tar xf deps/$POOL2 -C deps

