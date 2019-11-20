# Before building, verify `echo $CLASSPATH` is a directory that contains
# - jargors-GTreeJNI-1.0.0.jar (https://github.com/jargors/GTreeJNI)
# - jargors-Exceptions-1.0.0.jar (https://github.com/jargors/Exceptions)
VERSION=1.0.0
JAVASRCS = \
	java/Tools.java \
	java/Storage.java \
	java/Controller.java \
	java/Communicator.java \
	java/Traffic.java \
	java/Client.java \
	java/DesktopController.java

.PHONY : all src jar pdf purge

all : src jar pdf

src :
	make -f makesrc.mf

jar :
	make -f makejar.mf

pdf :
	make -f makepdf.mf

purge :
	rm -rf jargo.pdf com/ jar/jargors-$(VERSION).jar
	rm -rf doc/body.tex $(JAVASRCS)
	latexmk -f -c jargo.tex
