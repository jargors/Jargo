# Storage Correctness Test Generator
# - Set CLASSPATH environmental variable before running `make`.
# - BEWARE: `make clean` will not remove the generated test databases `db`
WIDGET=StorageCorrectnessTestGenerator

.PHONY : all clean

all : class

class : $(WIDGET).java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* $(WIDGET).java

clean :
	rm -f $(WIDGET).class
	rm -rf temp/
	rm -rf derby.log
