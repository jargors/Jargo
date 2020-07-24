java \
    -Xmx6g \
    -Djava.library.path=../dep \
    -cp .:../jar/*:../dep:../dep/*:$DERBY_HOME/lib/derby.jar \
com.github.jargors.test.ControllerTest $@
