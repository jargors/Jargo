<<<<<<< HEAD
# Storage Interface
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Storage
=======
# Simulation Controller
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Controller
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
VERSION=1.0.0

.PHONY : all clean

<<<<<<< HEAD
all : java tex pdf jar

jar : $(WIDGET).java
=======
all : pdf jar

jar : java
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* $(WIDGET).java
	jar cvf $(CLASSPATH)/jargors-$(WIDGET)-$(VERSION).jar com

java : src/$(WIDGET).nw
	notangle -R$(WIDGET).java src/$(WIDGET).nw > $(WIDGET).java

<<<<<<< HEAD
pdf : doc/$(WIDGET).tex
=======
pdf : tex
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
	pdflatex doc/$(WIDGET).tex

tex : src/$(WIDGET).nw
	noweave -delay -index src/$(WIDGET).nw > doc/$(WIDGET).tex

clean :
	rm -f $(WIDGET).pdf
	rm -f $(WIDGET).java
	rm -rf com/
	latexmk -f -c doc/$(WIDGET).tex
	rm -f doc/$(WIDGET).tex
