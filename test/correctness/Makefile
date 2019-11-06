# Storage Correctness Test
# - Set CLASSPATH environmental variable before running `make`.
WIDGET=StorageCorrectnessTest

.PHONY : all clean

all : java tex pdf class

class : $(WIDGET).java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* $(WIDGET).java

java : src/$(WIDGET).nw
	notangle -R$(WIDGET).java src/$(WIDGET).nw > $(WIDGET).java

pdf : doc/$(WIDGET).tex
	pdflatex doc/$(WIDGET).tex

tex : src/$(WIDGET).nw
	noweave -delay -index src/$(WIDGET).nw > doc/$(WIDGET).tex

clean :
	rm -f $(WIDGET).pdf
	rm -f $(WIDGET).java
	rm -f $(WIDGET).class
	latexmk -f -c doc/$(WIDGET).tex
	rm -f doc/$(WIDGET).tex
