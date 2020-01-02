# 'A_PeakExponential' Traffic Function
_WIDGET=A_PeakExponential
_A=.
_B=../../../jar/*
_C=commons-math3-3.6.1.jar
_CLASSPATH=$(_A):$(_B):$(_C)

.PHONY : all clean

_SRC=\
$(_WIDGET).java \
Num.java \
NormalDist.java \
TruncatedNormal.java

all : jargors-traffic-$(_WIDGET)-1.0.0.jar

jargors-traffic-$(_WIDGET)-1.0.0.jar : $(_SRC)
	javac \
	-Xlint:deprecation \
	-implicit:none \
	-d . \
	-cp $(_CLASSPATH) $(_SRC)
	jar cvf ../$@ com

clean :
	rm -rf com/
