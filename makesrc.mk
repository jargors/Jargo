# Get names of *.java files to be tangled by looking through src/
JAVA1=$(addsuffix .java, $(subst src/,java/,$(basename $(wildcard src/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/desktop/,java/desktop/,$(basename $(wildcard src/desktop/*.nw))))

.PHONY : all java tex clean

all : java tex

# Tangle the *.java files from the *.nw files
java : $(JAVA1) $(JAVA2)

# Weave body.tex from the *.nw files
tex : doc/body.tex

# Remove *.java and body.tex files
clean :
	rm -f $(JAVA1) $(JAVA2)
	rm -f doc/body.tex

################################################################################
# Just retangle all the noweb files for any java target
$(JAVA1) : src/*.nw src/*/*.nw
	#notangle -L'/*line %L "%F"*/%N' -R$(subst java/,,$@) src/*.nw src/*/*.nw > $@
	notangle -R$(subst java/,,$@) src/*.nw src/*/*.nw > $@

$(JAVA2) : src/*.nw src/*/*.nw
	#notangle -L'/*line %L "%F"*/%N' -R$(subst java/desktop/,,$@) src/*.nw src/*/*.nw > $@
	notangle -R$(subst java/desktop/,,$@) src/*.nw src/*/*.nw > $@

# Weave the *.nw files in the right order
TEXSRCS = \
	src/tex/Preface.nw \
	src/tex/Introduction.nw \
	src/tex/Building.nw \
	src/tex/GettingStarted.nw \
	src/tex/Overview.nw \
	src/tex/Reading.nw \
	src/tex/Writing.nw \
	src/tex/Administration.nw \
	src/tex/Gtree.nw \
	src/Storage.nw \
	src/Controller.nw \
	src/Communicator.nw \
	src/Client.nw \
	src/Traffic.nw \
	src/Tools.nw \
	src/desktop/DesktopController.nw \
	src/tex/JMX.nw \
	src/tex/DataDefinition.nw

doc/body.tex : src/*.nw src/*/*.nw
	noweave -delay -index $(TEXSRCS) > doc/body.tex