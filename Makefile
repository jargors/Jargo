# Storage Interface
# - Set JARGO_LIB environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $JARGO_LIB/.
#   Do that manually.
WIDGET=StorageInterface
VERSION=1.0.0

.PHONY : all clean

all : pdf jar

jar : java
	javac -Xlint:deprecation -d . $(WIDGET).java
	jar cvf $(JARGO_LIB)/jargors-$(WIDGET)-$(VERSION).jar com

java : src/$(WIDGET).nw
	notangle -R$(WIDGET).java src/$(WIDGET).nw > $(WIDGET).java

pdf : tex
	pdflatex doc/$(WIDGET).tex

tex : src/$(WIDGET).nw
	noweave -delay -index src/$(WIDGET).nw > doc/$(WIDGET).tex

clean :
	rm -f $(WIDGET).pdf
	rm -f $(WIDGET).java
	rm -rf com/
	latexmk -f -c doc/$(WIDGET).tex
	rm -f doc/$(WIDGET).tex
