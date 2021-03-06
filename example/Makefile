# This file describes how to build the Jargo example examples and traffic
# functions.

# Here I am getting the names of all the *.java files to be tangled.
JAVA1 = \
	java/client/NearestNeighbor.java \
	java/client/NearestPerpendicular.java \
	java/client/GreedyInsertion.java \
	java/client/GreedyInsertionFallback.java \

JAVA2 = \
	java/traffic/Broadway.java

# Here I am getting the names of all the *.class files to be compiled.
CLASS1=$(addsuffix .class, $(subst java/client/,com/github/jargors/client/,$(basename $(wildcard java/client/*.java))))
CLASS2=$(addsuffix .class, $(subst java/traffic/,com/github/jargors/traffic/,$(basename $(wildcard java/traffic/*.java))))

.PHONY : _mod all jar compile pdf src clean purge

# Print the menu.
_mod :
	@printf "_______________________________________________________________\n"
	@printf "Jargo Clients\n"
	@printf "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
	@printf "Commands:\n"
	@printf "  make all        build examples.jar + documentation\n"
	@printf "  make jar        build examples.jar only\n"
	@printf "  make pdf        build documentation only (pdf/examples.pdf)\n"
	@printf "  make src        build Java sources from Noweb files\n"
	@printf "  make clean      delete jar/, com/, pdf/, build.log\n"
	@printf "  make purge      clean + delete Java sources, doc/autogenerated.tex\n"
	@printf "===============================================================\n"

################################################################################
# These functions are used to keep terminal output clean during Java compilation.
# They redirect javac output into build.log. If javac returns with an error,
# then they dump build.log to the screen for the user to view. If javac returns
# a warning, in other words build.log is not empty, then they print a warning.
# These functions were modified from:
#     http://www.lunderberg.com/2015/08/25/cpp-makefile-pretty-output/
define run1
$(1) >> pdf/build.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat pdf/build.log; \
fi; \
exit $$RES
endef
define run2
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
all : jar pdf

# This target produces jar/examples.jar, along with com/.
jar : _prep src $(CLASS1) $(CLASS2)
	@printf "make examples.jar\n"
	@mkdir -p jar
	@printf "compress jar/examples.jar...\n"
	@jar cvf jar/examples.jar com >> build.log
	@printf "done examples.jar\n"

# This target produces pdf/examples.pdf, along with LaTeX objects.
pdf : _prep src doc/examples.tex doc/autogenerated.tex
	@printf "make pdf\n"
	@mkdir -p pdf
	@printf "compile pdf/examples.pdf...\n"
	@$(call run1, texfot pdflatex -output-directory pdf -halt-on-error doc/examples.tex)
	@printf "done pdf\n"

# This target produces all the Java files in java/ and doc/autogenerated.tex.
src : _precheck $(JAVA1) $(JAVA2) doc/autogenerated.tex

################################################################################
# Build Java files using 'notangle'.
$(JAVA1) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/client/,,$@) src/*.nw > $@

$(JAVA2) : src/*.nw
	@printf "tangle $@...\n"
	@notangle -R$(subst java/traffic/,,$@) src/*.nw > $@

# Build doc/autogenerated.tex.
TEXSRCS= \
	src/intro-tsp.nw \
	src/intro-pdp.nw \
	src/intro-rsp.nw \
	src/search-overview.nw \
	src/search-filters.nw \
	src/search-reductions.nw \
	src/search-scheduling.nw \
	src/search-schedules.nw \
	src/search-routing.nw \
	src/search-routes.nw \
	src/search-algorithms.nw \
	src/join-overview.nw \
	src/traffic-overview.nw \
	src/traffic-broadway.nw \
	src/utilities.nw \
	src/debug.nw

doc/autogenerated.tex : src/*.nw
	@printf "weave $@...\n"
	@noweave -delay -index $(TEXSRCS) > doc/autogenerated.tex

# This target describes how to compile the client Java sources in java/ into bytecode.
com/github/jargors/client/%.class : java/client/%.java
	@printf "compile $@...\n";
	@$(call run2, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:../jar/* $<)
#
# This target describes how to compile the traffic Java sources in java/ into bytecode.
com/github/jargors/traffic/%.class : java/traffic/%.java
	@printf "compile $@...\n";
	@$(call run2, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:../jar/* $<)

clean :
	@printf "make clean\n"
	@if [ -d "jar" ]; then printf "remove jar/...\n"; rm -rf jar/ ; fi; \
	 if [ -d "com" ]; then printf "remove com/...\n"; rm -rf com/ ; fi; \
	 if [ -d "pdf" ]; then printf "remove pdf/...\n"; rm -rf pdf/ ; fi; \
	 if [ -s "build.log" ]; then printf "remove build.log...\n"; rm -rf build.log ; fi;
	@printf "done clean\n"

# In addition to 'clean', remove Java files in java/ and doc/autogenerated.tex.
purge : clean
	@printf "make purge\n"
	@printf "purge java...\n"; rm -rf java/; \
	 if [ -f "doc/autogenerated.tex" ]; then printf "purge doc/autogenerated.tex...\n"; rm -f doc/autogenerated.tex ; fi;
	@printf "done purge\n"

# Remove any existing com/, build.log, pdf/build.log.
_prep :
	@rm -rf com/ build.log pdf/build.log

_precheck :
	@mkdir -p java/client java/traffic

