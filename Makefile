# Before building, verify `echo $CLASSPATH` is a directory that contains
# - jargors-GTreeJNI-1.0.0.jar (https://github.com/jargors/GTreeJNI)
# - jargors-Exceptions-1.0.0.jar (https://github.com/jargors/Exceptions)
VERSION = 1.0.0
SRCS = \
	src/0-Preface.nw \
	src/0-Overview.nw \
	src/1-Introduction.nw \
	src/2-Reading.nw \
	src/3-Writing.nw \
	src/4-Administration.nw \
	src/5-Gtree.nw \
	src/Storage.nw \
	src/Controller.nw \
	src/Communicator.nw \
	src/Traffic.nw \
	src/Client.nw \
	src/Tools.nw \
	src/DesktopController.nw
JAVASRCS = \
	java/Tools.java \
	java/Storage.java \
	java/Controller.java \
	java/Communicator.java \
	java/Traffic.java \
	java/Client.java \
	java/DesktopController.java
CLASSES = \
	com/github/jargors/Tools.class \
	com/github/jargors/Storage.class \
	com/github/jargors/Controller.class \
	com/github/jargors/Communicator.class \
	com/github/jargors/Traffic.class \
	com/github/jargors/Client.class \
	com/github/jargors/DesktopController.class

.PHONY : all java compile tex clean purge

all : java compile jar tex pdf

# This target compiles all the java/*.java files from src/*.nw files
java : $(JAVASRCS)

# This target compiles all the com/../*.class files from java/*.java files
compile : $(CLASSES) com/github/jargors/Desktop.class

# This target zips up com/ into jar/jargors-*.jar
jar : $(CLASSES) com/github/jargors/Desktop.class
	jar cvf jar/jargors-$(VERSION).jar com

# This target compiles doc/body.tex from src/*nw files
tex : $(SRCS)
	noweave -delay -index $(SRCS) > doc/body.tex

# This target compiles jargo.pdf from jargo.tex
pdf : tex
	texfot pdflatex -halt-on-error jargo.tex

################################################################################
$(JAVASRCS) : java/%.java: src/%.nw
	notangle -R$(subst java/,,$@) $(SRCS) > $@

# If any SRCS change (not just pattern-matched sources), then rebuild JAVASRCS
$(JAVASRCS) : $(SRCS)

$(CLASSES) : $(JAVASRCS)
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:$(CLASSPATH)/* $(JAVASRCS)

com/github/jargors/Desktop.class : java/Desktop.java
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:$(CLASSPATH)/* $<

clean :
	rm -rf jargo.pdf com/ jar/jargors-$(VERSION).jar
	latexmk -f -c jargo.tex

purge : clean
	rm -rf doc/body.tex $(JAVASRCS)
