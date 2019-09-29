# Storage
.PHONY : all clean

all : pdf jar

jar : java
	javac -d . Storage.java
	jar cvf $(JARGO_LIB)/jargors-storage-1.0.0.jar com

java : src/storage.nw
	notangle -RStorage.java src/storage.nw > Storage.java

pdf : tex
	pdflatex doc/storage.tex

tex : src/storage.nw
	noweave -delay -index src/storage.nw > doc/storage.tex

clean :
	rm -f storage.pdf
	rm -f Storage.ja* jargors-storage-*.jar
	rm -rf com/
	latexmk -f -c doc/storage.tex
	rm -f doc/storage.tex
