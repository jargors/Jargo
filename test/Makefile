# Storage Test
# - Set JARGO_LIB environmental variable before running `make`.
WIDGET=StorageInterfaceTest

.PHONY : all clean

all : pdf class

class : java
	javac -Xlint:deprecation -d . -cp .:$(JARGO_LIB)/* $(WIDGET).java

java : src/$(WIDGET).nw
	notangle -R$(WIDGET).java src/$(WIDGET).nw > $(WIDGET).java

pdf : tex
	pdflatex doc/$(WIDGET).tex

tex : src/$(WIDGET).nw
	noweave -delay -index src/$(WIDGET).nw > doc/$(WIDGET).tex

clean :
	rm -f $(WIDGET).pdf
	rm -f $(WIDGET).java
	rm -f $(WIDGET).class
	latexmk -f -c doc/$(WIDGET).tex
	rm -f doc/$(WIDGET).tex
