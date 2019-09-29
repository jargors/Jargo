# Storage Test
# - Set JARGO_LIB environmental variable before running `make`.
.PHONY : all clean

all : pdf class

class : java
	javac -Xlint:deprecation -d . -cp .:$(JARGO_LIB)/* StorageTest.java

java : src/storagetest.nw
	notangle -RStorageTest.java src/storagetest.nw > StorageTest.java

pdf : tex
	pdflatex doc/storagetest.tex

tex : src/storagetest.nw
	noweave -delay -index src/storagetest.nw > doc/storagetest.tex

clean :
	rm -f storagetest.pdf
	rm -f StorageTest.java
	rm -f StorageTest.class
	latexmk -f -c doc/storagetest.tex
	rm -f doc/storagetest.tex
