# Storage Test Generator
# - Set CLASSPATH environmental variable before running `make`.
# - BEWARE: `make clean` will remove the generated test databases `db`, `temp`
WIDGET=StorageTestGenerator

.PHONY : all clean

all : class

class : $(WIDGET).java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* $(WIDGET).java

clean :
	rm -f $(WIDGET).class
	rm -rf db/
	rm -rf temp/
	rm -rf derby.log
