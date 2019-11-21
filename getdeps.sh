#!/usr/bin/env bash
OPENJFX=openjfx-13.0.1_linux-x64_bin-sdk.zip
DBCP2=commons-dbcp2-2.7.0-bin.tar.gz
LOGGING=commons-logging-1.2-bin.tar.gz
POOL2=commons-pool2-2.7.0-bin.tar.gz
GTREE=jargors-GTreeJNI-1.0.0.tar.gz

mkdir -p deps/src
curl https://download2.gluonhq.com/openjfx/13.0.1/$OPENJFX --output deps/src/$OPENJFX
curl http://mirror.navercorp.com/apache//commons/dbcp/binaries/$DBCP2 --output deps/src/$DBCP2
curl http://mirror.navercorp.com/apache//commons/logging/binaries/$LOGGING --output deps/src/$LOGGING
curl http://apache.mirror.cdnetworks.com//commons/pool/binaries/$POOL2 --output deps/src/$POOL2
curl -L https://github.com/jargors/GTreeJNI/releases/download/1.0.0/$GTREE --output deps/src/$GTREE

unzip deps/src/$OPENJFX -d deps/
tar xf deps/src/$DBCP2 -C deps
tar xf deps/src/$LOGGING -C deps
tar xf deps/src/$POOL2 -C deps
tar xf deps/src/$GTREE -C deps

mv deps/javafx-sdk-13.0.1/lib/*.jar deps/.
mv deps/javafx-sdk-13.0.1/lib/*.so deps/.
mv deps/commons-dbcp2-2.7.0/*.jar deps/.
mv deps/commons-logging-1.2/*.jar deps/.
mv deps/commons-pool2-2.7.0/*.jar deps/.

rm -rf deps/javafx-sdk-13.0.1
rm -rf deps/commons-dbcp2-2.7.0
rm -rf deps/commons-logging-1.2
rm -rf deps/commons-pool2-2.7.0

