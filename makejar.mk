# This file describes how to compile the Java files in the java/ directory into
# Java bytecode and to compress the bytecode into a jar archive. The outputs of
# the make commands are the class files in the com/ directory and the jar
# library in the jar/ directory.

# Make sure to update this variable for new releases.
VERSION = 1.0.0

# Here I am getting the names of all the *.class files to be compiled.
CLASS1=$(addsuffix .class, $(subst java/core/,com/github/jargors/core/,$(basename $(wildcard java/core/*.java))))
CLASS2=$(addsuffix .class, $(subst java/gui/,com/github/jargors/gui/,$(basename $(wildcard java/gui/*.java))))
CLASS3=$(addsuffix .class, $(subst java/jmx/,com/github/jargors/jmx/,$(basename $(wildcard java/jmx/*.java))))
CLASS4=$(addsuffix .class, $(subst java/cli/,com/github/jargors/cli/,$(basename $(wildcard java/cli/*.java))))

.PHONY : all compile jar clean _prep

# This target produces com/ and jar/jargors-VERSION.jar, along with build.log.
all : compile jar _prep

# This target produces all the Java bytecode in com/, along with build.log.
compile : _prep $(CLASS1) $(CLASS2) $(CLASS3) $(CLASS4)

# This target produces jar/jargors-VERSION.jar and build.log.
jar : compile
	@mkdir -p jar
	@printf "compress jar/jargors-$(VERSION).jar...\n"
	@jar cvf jar/jargors-$(VERSION).jar com >> build.log

# Remove com/ and jar/.
clean :
	@rm -rf com/
	@rm -rf jar/

# The below instructions explain how to compile the class files.
################################################################################
# This function is used to keep terminal output clean during Java compilation.
# It redirects javac output into build.log. If javac returns with an error,
# then it dumps build.log to the screen for the user to view. If javac returns
# a warning, in other words build.log is not empty, then it prints a warning.
# This function was modified from:
#     http://www.lunderberg.com/2015/08/25/cpp-makefile-pretty-output/
define run
$(1) >> build.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat build.log; \
elif [ -s build.log ]; then \
	printf "\twarning! check 'build.log' for details...\n"; \
fi; \
exit $$RES
endef
################################################################################
# Some classes use code chunks from multiple *.java files, so I just pass all
# the java files as arguments to javac.
com/github/jargors/core/%.class : java/core/%.java
	@printf "compile java/core sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*/*.java)

com/github/jargors/gui/%.class : java/gui/%.java
	@printf "compile java/gui sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*/*.java)

com/github/jargors/jmx/%.class : java/jmx/%.java
	@printf "compile java/jmx sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*/*.java)

com/github/jargors/cli/%.class : java/cli/%.java
	@printf "compile java/cli sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*/*.java)

# Delete any existing com/ and build.log so our outputs are clean.
_prep :
	@rm -rf com/
	@rm -rf build.log

