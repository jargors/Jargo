# Get names of *.class files to be compiled by looking through java/
CLASS1=$(addsuffix .class, $(subst java/,com/github/jargors/,$(basename $(wildcard java/*.java))))
CLASS2=$(addsuffix .class, $(subst java/desktop/,com/github/jargors/desktop/,$(basename $(wildcard java/desktop/*.java))))
CLASS3=$(addsuffix .class, $(subst java/exceptions/,com/github/jargors/exceptions/,$(basename $(wildcard java/exceptions/*.java))))
CLASS4=$(addsuffix .class, $(subst java/jmx/,com/github/jargors/jmx/,$(basename $(wildcard java/jmx/*.java))))

VERSION = 1.0.0

.PHONY : all compile jar clean

all : compile jar

# Compile the *.class files from the *.java files
compile : $(CLASS1) $(CLASS2) $(CLASS3) $(CLASS4)

# Zip up *.class files into jar/jargors-$(VERSION).jar
jar : compile
	mkdir -p jar
	jar cvf jar/jargors-$(VERSION).jar com

# Remove *.class and *.jar files
clean :
	rm -rf com/ jar/jargors-$(VERSION).jar

################################################################################
# Just recompile all the java files for any class target
com/github/jargors/%.class : java/%.java
	rm -rf com/
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:deps:deps/* java/*.java java/*/*.java

com/github/jargors/desktop/%.class : java/desktop/%.java
	rm -rf com/
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:deps:deps/* java/*.java java/*/*.java

com/github/jargors/exceptions/%.class : java/exceptions/%.java
	rm -rf com/
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:deps:deps/* java/*.java java/*/*.java

com/github/jargors/jmx/%.class : java/jmx/%.java
	rm -rf com/
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:deps:deps/* java/*.java java/*/*.java