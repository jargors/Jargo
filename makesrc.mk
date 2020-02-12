# This file describes how to compile the noweb files in the src/ directory into
# Java and LaTeX code. The outputs of the make commands are the Java files in
# the java/ directory and doc/body.tex. IMPORTANT: the 'all', 'java', and 'tex'
# targets require noweb programs 'notangle' and 'noweave' in your PATH.
# Additionally, noweb must be compiled using the icont option (instead of awk).

# Here I am getting the names of the *.java files to be tangled by looking for
# all the noweb files in src/. None of the files in tex/ produce Java classes,
# so that subdirectory is omitted.
JAVA1=$(addsuffix .java, $(subst src/core/,java/core/,$(basename $(wildcard src/core/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/gui/,java/gui/,$(basename $(wildcard src/gui/*.nw))))
JAVA3=$(addsuffix .java, $(subst src/cli/,java/cli/,$(basename $(wildcard src/cli/*.nw))))

.PHONY : all java tex clean

# This target produces all the Java files in java/ and also doc/body.tex.
all : java tex

# This target produces all the Java files in java/.
java : $(JAVA1) $(JAVA2) $(JAVA3)

# This target produces doc/body.tex.
tex : doc/body.tex

# Remove *.java and doc/body.tex files
clean :
	@rm -f $(JAVA1) $(JAVA2) $(JAVA3)
	@rm -f doc/body.tex

# The below instructions explain how to build Java files and doc/body.tex.
################################################################################
# Build Java files using 'notangle'. Some classes use code chunks from multiple
# *.nw files, so I just pass all the noweb files as arguments to notangle.
$(JAVA1) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/core/,,$@) src/*/*.nw > $@

$(JAVA2) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/gui/,,$@) src/*/*.nw > $@

$(JAVA3) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/cli/,,$@) src/*/*.nw > $@

# Build doc/body.tex using 'noweave'. The order of the noweave arguments
# matters, so I use a temporary variable TEXSRCS to establish the order.
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
	src/core/Storage.nw \
	src/core/Controller.nw \
	src/core/Communicator.nw \
	src/core/Client.nw \
	src/core/Traffic.nw \
	src/core/Tools.nw \
	src/cli/Command.nw \
	src/gui/DesktopController.nw \
	src/tex/JMX.nw \
	src/tex/DataDefinition.nw

# Here, the -delay option disables automatic preamble (I use doc/jargo.tex
# instead), and the -index option creates hyperlink references to chunks.
doc/body.tex : src/*/*.nw
	@printf "weave $@...\n"
	@noweave -delay -index $(TEXSRCS) > doc/body.tex

