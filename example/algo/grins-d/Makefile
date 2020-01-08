# GRINS-d Algorithm
_WIDGET=GreedyDelta
_A=.
_B=../../../jar/*
_CLASSPATH=$(_A):$(_B)

.PHONY : clean

jargors-algo-$(_WIDGET)-1.0.0.jar : $(_WIDGET).java
	javac \
	-Xlint:deprecation \
	-implicit:none \
	-d . \
	-cp $(_CLASSPATH) $<
	jar cvf ../$@ com

clean :
	rm -rf com/
