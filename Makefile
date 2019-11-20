# Get names of *.java files by looking through src/
JAVA1=$(addsuffix .java, $(subst src/,java/,$(basename $(wildcard src/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/desktop/,java/desktop/,$(basename $(wildcard src/desktop/*.nw))))

VERSION=1.0.0

.PHONY : all src jar pdf purge

all : src jar pdf

src :
	make -f makesrc.mf

jar :
	make -f makejar.mf

pdf :
	make -f makepdf.mf

purge :
	rm -f jargo.pdf jar/jargors-$(VERSION).jar
	rm -f doc/body.tex $(JAVA1) $(JAVA2)
	rm -rf com/ 
	latexmk -f -c jargo.tex
