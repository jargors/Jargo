# This file describes how to compile the noweb files in the src/ directory into
# Java and LaTeX code. The outputs of the make commands are the Java files in
# the java/ directory and doc/body.tex. IMPORTANT: the 'all', 'java', and 'tex'
# targets require noweb programs 'notangle' and 'noweave' in your PATH.
# Additionally, noweb must be compiled using the icont option (instead of awk).

# Here I am getting the names of the *.java files to be tangled by looking for
# all the noweb files in src/. None of the files in tex/ produce Java classes,
# so that subdirectory is omitted.
JAVA1=$(addsuffix .java, $(subst src/class/,java/class/,$(basename $(wildcard src/class/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/ui/,java/ui/,$(basename $(wildcard src/ui/*.nw))))

# Check if the user has the required build tools (poor man's autoconf). The
# 'command' command should work on all POSIX systems.
bin_noweave:=$(shell command -v noweave 2> /dev/null)
bin_notangle:=$(shell command -v notangle 2> /dev/null)

.PHONY : _precheck all java tex clean

# This target produces all the Java files in java/ and also doc/body.tex.
# (most people are going to use 'src' target of Makefile, causing 'all' target
# of this file to be built. The _precheck is added to this target. Running
# 'make src java' or 'make src tex' will skip the _precheck.)
all : _precheck java tex

# This target produces all the Java files in java/.
java : $(JAVA1) $(JAVA2)

# This target produces doc/body.tex.
tex : doc/body.tex

# Remove *.java and doc/body.tex files
clean :
	@rm -f $(JAVA1) $(JAVA2)
	@rm -f doc/body.tex

# The below instructions explain how to build Java files and doc/body.tex.
################################################################################
# If the user doesn't have noweb, throw and error and stop.
_precheck :
ifndef bin_noweave
	$(error "**ERROR: 'noweave' not found! target 'src' unavailable.")
endif
ifndef bin_notangle
	$(error "**ERROR: 'notangle' not found! target 'src' unavailable.")
endif

# Build Java files using 'notangle'. Some classes use code chunks from multiple
# *.nw files, so I just pass all the noweb files as arguments to notangle.
$(JAVA1) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/class/,,$@) src/*/*.nw > $@

$(JAVA2) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/ui/,,$@) src/*/*.nw > $@

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
	src/class/Storage.nw \
	src/class/Controller.nw \
	src/class/Communicator.nw \
	src/class/Client.nw \
	src/class/Traffic.nw \
	src/class/Tools.nw \
	src/ui/Command.nw \
	src/ui/DesktopController.nw \
	src/tex/JMX.nw \
	src/tex/DataDefinition.nw

# Here, the -delay option disables automatic preamble (I use doc/jargo.tex
# instead), and the -index option creates hyperlink references to chunks.
doc/body.tex : src/*/*.nw
	@printf "weave $@...\n"
	@noweave -delay -index $(TEXSRCS) > doc/body.tex

