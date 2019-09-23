all : storagetest.pdf StorageTest.class
.PHONY : all clean cleantex cleandoc cleanjava cleanclass

StorageTest.class : StorageTest.java
	javac -Xlint:deprecation -d . -cp ../jargors-storage-1.0.0.jar StorageTest.java

StorageTest.java : src/storagetest.nw
	notangle -RStorageTest.java src/storagetest.nw > StorageTest.java

storagetest.pdf : doc/storagetest.tex
	pdflatex doc/storagetest.tex

doc/storagetest.tex : src/storagetest.nw
	noweave -delay -index src/storagetest.nw > doc/storagetest.tex

clean : cleanjava cleanclass cleantex cleandoc
	rm -f storagetest.pdf

cleanjava :
	rm -f StorageTest.java

cleanclass :
	rm -f StorageTest.class

cleantex :
	latexmk -f -c doc/storagetest.tex

cleandoc :
	rm -f doc/storagetest.tex
