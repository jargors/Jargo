# 'Random' Traffic Function
_WIDGET=RandomTraffic
_A=.
_B=../../../jar/*
_CLASSPATH=$(_A):$(_B)

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
	rm -f $(_WIDGET).pdf
	rm -rf com/
	latexmk -f -c doc/$(_WIDGET).tex
