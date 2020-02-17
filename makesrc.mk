# This file describes how to compile the noweb files in the src/ directory into
# Java and LaTeX code. The outputs of the make commands are the Java files in
# the java/ directory and doc/body.tex. IMPORTANT: the 'all', 'java', and 'tex'
# targets require noweb programs 'notangle' and 'noweave' in your PATH.
# Additionally, noweb must be compiled using the icont option (instead of awk).

# Here I am getting the names of the *.java files to be tangled by looking for
# all the noweb files in src/.
JAVA1=$(addsuffix .java, $(subst src/core/,java/core/,$(basename $(wildcard src/core/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/ui/,java/ui/,$(basename $(wildcard src/ui/*.nw))))

# The Exception classes are not written in their own noweb files, so I can't
# look through the src/ directory for them. Instead I have to list them by hand.
# I can search for them by using: 'grep "Exception.java" src/*/*'. All
# exceptions belong to the core package, so they go into java/core/.
JAVA3 = \
	java/core/ClientException.java \
	java/core/ClientFatalException.java \
	java/core/DuplicateEdgeException.java \
	java/core/DuplicateUserException.java \
	java/core/DuplicateVertexException.java \
	java/core/EdgeNotFoundException.java \
	java/core/GtreeIllegalSourceException.java \
	java/core/GtreeIllegalTargetException.java \
	java/core/GtreeNotLoadedException.java \
	java/core/RouteIllegalOverwriteException.java \
	java/core/TimeWindowException.java \
	java/core/UserNotFoundException.java \
	java/core/VertexNotFoundException.java

# Similarly, JMX classes are not written in their own noweb files. They
# are located in tex/JMX.nw. These classes belong to the jmx package.
JAVA4 = \
	java/jmx/ClientMonitor.java \
	java/jmx/ClientMonitorMBean.java \
	java/jmx/CommunicatorMonitor.java \
	java/jmx/CommunicatorMonitorMBean.java \
	java/jmx/ControllerMonitor.java \
	java/jmx/ControllerMonitorMBean.java \
	java/jmx/StorageMonitor.java \
	java/jmx/StorageMonitorMBean.java

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
java : $(JAVA1) $(JAVA2) $(JAVA3) $(JAVA4)

# This target produces doc/body.tex.
tex : doc/body.tex

# Remove *.java and doc/body.tex files
clean :
	@rm -f $(JAVA1) $(JAVA2) $(JAVA3) $(JAVA4)
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
	@mkdir -p java/core java/ui java/jmx

# Build Java files using 'notangle'. Some classes use code chunks from multiple
# *.nw files, so I just pass all the noweb files as arguments to notangle.
$(JAVA1) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/core/,,$@) src/*/*.nw > $@

$(JAVA2) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/ui/,,$@) src/*/*.nw > $@

$(JAVA3) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/core/,,$@) src/*/*.nw > $@

$(JAVA4) : src/*/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/jmx/,,$@) src/*/*.nw > $@

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
	src/ui/Command.nw \
	src/ui/Desktop.nw \
	src/ui/DesktopController.nw \
	src/tex/JMX.nw \
	src/tex/DataDefinition.nw

# Here, the -delay option disables automatic preamble (I use doc/jargo.tex
# instead), and the -index option creates hyperlink references to chunks.
doc/body.tex : src/*/*.nw
	@printf "weave $@...\n"
	@noweave -delay -index $(TEXSRCS) > doc/body.tex

