# Examples app
#_CLASSPATH=.:../jar/*:../deps:../deps/*:./algo/*:./traffic/*
_CLASSPATH=.:../jar/*:algo/*:traffic/*

.PHONY : all class clean

all : class

class : app.java
	javac \
	-Xlint:deprecation \
	-implicit:none \
	-d . \
	-cp $(_CLASSPATH) app.java

clean :
	rm -f app.class
