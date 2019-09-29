# Storage
all : storage.pdf jargors-storage-1.0.0.jar
.PHONY : all clean cleantex cleandoc cleanjava cleanclass

jargors-storage-1.0.0.jar : com/github/jargors/Storage.class
	jar cvf jargors-storage-1.0.0.jar com

com/github/jargors/Storage.class : Storage.java
	javac -d . -cp . Storage.java

Storage.java : src/storage.nw
	notangle -RStorage.java src/storage.nw > Storage.java

storage.pdf : doc/storage.tex
	pdflatex doc/storage.tex

doc/storage.tex : src/storage.nw
	noweave -delay -index src/storage.nw > doc/storage.tex

clean : cleanjava cleanclass cleantex cleandoc
	rm -f storage.pdf

cleanjava :
	rm -f Storage.ja* jargors-storage-*.jar

cleanclass :
	rm -rf com/

cleantex :
	latexmk -f -c doc/storage.tex

cleandoc :
	rm doc/storage.tex
