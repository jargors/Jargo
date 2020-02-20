# This file describes how to compile the noweb files in the src/ directory into
# Java and LaTeX code. The outputs of the make commands are the Java files in
# the java/ directory and doc/body.tex. IMPORTANT: the 'all', 'java', and 'tex'
# targets require noweb programs 'notangle' and 'noweave' in your PATH.
# Additionally, noweb must be compiled using the icont option (instead of awk).

# Here I am getting the names of the *.java files to be tangled.  The main
# simulation classes belong to the sim package.
JAVA1 = \
	java/sim/Client.java \
	java/sim/Controller.java \
	java/sim/Communicator.java \
	java/sim/Storage.java \
	java/sim/Tools.java

# The interface classes belong to the ui package.
JAVA2 = \
	java/ui/Command.java \
	java/ui/Desktop.java \
	java/ui/DesktopController.java

# The Exceptions classes belong to the sim package.
JAVA3 = \
	java/sim/ClientException.java \
	java/sim/ClientFatalException.java \
	java/sim/DuplicateEdgeException.java \
	java/sim/DuplicateUserException.java \
	java/sim/DuplicateVertexException.java \
	java/sim/EdgeNotFoundException.java \
	java/sim/GtreeIllegalSourceException.java \
	java/sim/GtreeIllegalTargetException.java \
	java/sim/GtreeNotLoadedException.java \
	java/sim/RouteIllegalOverwriteException.java \
	java/sim/TimeWindowException.java \
	java/sim/UserNotFoundException.java \
	java/sim/VertexNotFoundException.java

# The JMX classes are located in tex/JMX.nw. These classes belong to the jmx
# package.
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
	@mkdir -p java/sim java/ui java/jmx

# Build Java files using 'notangle'. Some classes use code chunks from multiple
# *.nw files, so I just pass all the noweb files as arguments to notangle.
$(JAVA1) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/sim/,,$@) src/*.nw > $@

$(JAVA2) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/ui/,,$@) src/*.nw > $@

$(JAVA3) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/sim/,,$@) src/*.nw > $@

$(JAVA4) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/jmx/,,$@) src/*.nw > $@

# Build doc/body.tex using 'noweave'. The order of the noweave arguments
# matters, so I use a temporary variable TEXSRCS to establish the order.
TEXSRCS = \
	src/preface.nw \
	src/tut-install.nw \
	src/tut-example.nw \
	src/tut-start.nw \
	src/mod-setting.nw \
	src/mod-users.nw \
	src/mod-metrics.nw \
	src/mod-schema.nw \
	src/sim-overview.nw \
	src/sim-reading.nw \
	src/sim-writing.nw \
	src/sim-administration.nw \
	src/sim-gtree.nw \
	src/sim-jmx.nw \
	src/sim-client.nw \
	src/sim-communicator.nw \
	src/sim-controller.nw \
	src/sim-storage.nw \
	src/sim-tools.nw \
	src/ui-command.nw \
	src/ui-guiapp.nw \
	src/ui-guicontroller.nw

# Here, the -delay option disables automatic preamble (I use doc/jargo.tex
# instead), and the -index option creates hyperlink references to chunks.
doc/body.tex : src/*.nw
	@printf "weave $@...\n"
	@noweave -delay -index $(TEXSRCS) > doc/body.tex

