# Get names of *.java files by looking through src/
JAVA1=$(addsuffix .java, $(subst src/,java/,$(basename $(wildcard src/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/desktop/,java/desktop/,$(basename $(wildcard src/desktop/*.nw))))

VERSION=1.0.0

.PHONY : all src jar pdf purge clean

all : src jar pdf

src :
	make -f makesrc.mk

jar : src
	make -f makejar.mk

pdf : src
	make -f makepdf.mk

clean :
	rm -f jargo.pdf jar/jargors-$(VERSION).jar
	rm -rf com/
	latexmk -f -c jargo.tex

purge : clean
	rm -f $(JAVA1) $(JAVA2)
	rm -f doc/body.tex
