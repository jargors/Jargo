# 'Broadway' Traffic Function
_WIDGET=Broadway
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
	rm -rf com/
