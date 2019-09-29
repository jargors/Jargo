# Storage
# - Set JARGO_LIB environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $JARGO_LIB/.
#   Do that manually.
.PHONY : all clean

all : pdf jar

jar : java
	javac -Xlint:deprecation -d . Storage.java
	jar cvf $(JARGO_LIB)/jargors-storage-1.0.0.jar com

java : src/storage.nw
	notangle -RStorage.java src/storage.nw > Storage.java

pdf : tex
	pdflatex doc/storage.tex

tex : src/storage.nw
	noweave -delay -index src/storage.nw > doc/storage.tex

clean :
	rm -f storage.pdf
	rm -f Storage.java
	rm -rf com/
	latexmk -f -c doc/storage.tex
	rm -f doc/storage.tex
