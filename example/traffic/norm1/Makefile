# 'Norm1' Traffic Function
_WIDGET=Norm1
_A=.
_B=../../../jar/*
_C=commons-math3-3.6.1.jar
_CLASSPATH=$(_A):$(_B):$(_C)

.PHONY : all clean

all : jargors-traffic-$(_WIDGET)-1.0.0.jar

jargors-traffic-$(_WIDGET)-1.0.0.jar : $(_WIDGET).java
	javac \
	-Xlint:deprecation \
	-implicit:none \
	-d . \
	-cp $(_CLASSPATH) $<
	jar cvf ../$@ com

clean :
	rm -rf com/
