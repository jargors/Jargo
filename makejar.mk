# Get names of *.class files to be compiled by looking through java/
CLASS1=$(addsuffix .class, $(subst java/,com/github/jargors/,$(basename $(wildcard java/*.java))))
CLASS2=$(addsuffix .class, $(subst java/desktop/,com/github/jargors/desktop/,$(basename $(wildcard java/desktop/*.java))))
CLASS3=$(addsuffix .class, $(subst java/exceptions/,com/github/jargors/exceptions/,$(basename $(wildcard java/exceptions/*.java))))
CLASS4=$(addsuffix .class, $(subst java/jmx/,com/github/jargors/jmx/,$(basename $(wildcard java/jmx/*.java))))

VERSION = 1.0.0

.PHONY : all compile jar clean _prep

all : compile jar _prep

# Compile the *.class files from the *.java files
compile : _prep $(CLASS1) $(CLASS2) $(CLASS3) $(CLASS4)

# Zip up *.class files into jar/jargors-$(VERSION).jar
jar : compile
	@mkdir -p jar
	@printf "compress jar/jargors-$(VERSION).jar...\n"
	@jar cvf jar/jargors-$(VERSION).jar com >> build.log

# Remove *.class and *.jar files
clean :
	@rm -rf com/
	@rm -rf jar/

################################################################################
# http://www.lunderberg.com/2015/08/25/cpp-makefile-pretty-output/
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
# Just recompile all the java files for any class target
com/github/jargors/%.class : java/%.java
	@printf "compile java sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*.java java/*/*.java)

com/github/jargors/desktop/%.class : java/desktop/%.java
	@printf "compile java sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*.java java/*/*.java)

com/github/jargors/exceptions/%.class : java/exceptions/%.java
	@printf "compile java sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*.java java/*/*.java)

com/github/jargors/jmx/%.class : java/jmx/%.java
	@printf "compile java sources...\n";
	@$(call run, javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:dep:dep/* java/*.java java/*/*.java)

_prep :
	@rm -rf com/
	@rm -rf build.log

