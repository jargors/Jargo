# Desktop
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Desktop
VERSION=1.0.0

.PHONY : all clean

all : pdf class

class : java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* --module-path $(CLASSPATH) --add-modules javafx.controls $(WIDGET).java

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
