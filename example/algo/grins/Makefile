# GRINS Algorithm
_WIDGET=GreedyInsertion
_A=.
_B=../../../jar/*
_CLASSPATH=$(_A):$(_B)

.PHONY : all clean purge

all : $(_WIDGET).pdf jargors-algo-$(_WIDGET)-1.0.0.jar

jargors-algo-$(_WIDGET)-1.0.0.jar : $(_WIDGET).java
	javac \
	-Xlint:deprecation \
	-implicit:none \
	-d . \
	-cp $(_CLASSPATH) $<
	jar cvf ../$@ com

$(_WIDGET).java : src/$(_WIDGET).nw
	notangle -R$@ $< > $@

$(_WIDGET).pdf : doc/$(_WIDGET).tex
	pdflatex $<

doc/$(_WIDGET).tex : src/$(_WIDGET).nw
	mkdir -p doc
	noweave -delay -index $< > $@

clean :
	rm -f $(_WIDGET).pdf
	rm -rf com/
	latexmk -f -c doc/$(_WIDGET).tex

purge : clean
	rm -rf $(_WIDGET).java
	rm -rf doc/$(_WIDGET).tex
