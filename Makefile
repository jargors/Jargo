# Before building, verify `echo $CLASSPATH` is a directory that contains
# - jargors-GTreeJNI-1.0.0.jar (https://github.com/jargors/GTreeJNI)
# - jargors-Exceptions-1.0.0.jar (https://github.com/jargors/Exceptions)
VERSION = 1.0.0
JAVASRCS = \
	java/Tools.java \
	java/Storage.java \
	java/Controller.java \
	java/Communicator.java \
	java/Client.java \
	java/DesktopController.java \
	java/Traffic.java
CLASSES = \
	com/github/jargors/Tools.class \
	com/github/jargors/Storage.class \
	com/github/jargors/Controller.class \
	com/github/jargors/Communicator.class \
	com/github/jargors/Client.class \
	com/github/jargors/DesktopController.class \
	com/github/jargors/Traffic.class
TEXSRCS = \
	doc/Tools.tex \
	doc/Storage.tex \
	doc/Controller.tex \
	doc/Communicator.tex \
	doc/Client.tex \
	doc/DesktopController.tex \
	doc/Traffic.tex

.PHONY : all java compile tex clean purge

all : java compile jar tex pdf

java : $(JAVASRCS)

compile : $(CLASSES)

tex : $(TEXSRCS)

$(JAVASRCS): java/%.java: src/%.nw
	@notangle -R$(subst java/,,$@) $< > $@

$(TEXSRCS) : doc/%.tex : src/%.nw
	@noweave -delay -index $< > $@

$(CLASSES) : com/github/jargors/%.class: java/%.java
	@javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:$(CLASSPATH)/* $<

jar : $(CLASSES)
	@jar cvf jar/jargors-$(VERSION).jar com

pdf : $(TEXSRCS)
	@texfot pdflatex -halt-on-error jargo.tex

clean :
	@rm -rf jargo.pdf com/ jar/jargors-$(VERSION).jar
	@latexmk -f -c jargo.tex

purge : clean
	@rm -rf doc/*.tex java/*.java
