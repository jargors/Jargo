<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
# Storage Interface
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Storage
=======
# Simulation Controller
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Controller
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
=======
# Simulation Interface
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Communicator
>>>>>>> e4903e6712f100d8acd875fb96c63dff0b780d8e
=======
# Base Client
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=Client
>>>>>>> 07460a53ab04f3b98d0f84bd7e8ac3ecea017ba1
=======
# Desktop
# - Set CLASSPATH environmental variable before running `make`.
# - Command `make clean` does NOT remove the compressed jar from $CLASSPATH/.
#   Do that manually.
WIDGET=DesktopController
>>>>>>> 30fea70ff88428ba48b47386584487936844d1ae
VERSION=1.0.0

.PHONY : all clean

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
all : java tex pdf jar

jar : $(WIDGET).java
=======
all : pdf jar

jar : java
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
=======
all : pdf jar

jar : java
>>>>>>> e4903e6712f100d8acd875fb96c63dff0b780d8e
=======
all : pdf jar

jar : java
>>>>>>> 07460a53ab04f3b98d0f84bd7e8ac3ecea017ba1
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* $(WIDGET).java
	jar cvf $(CLASSPATH)/jargors-$(WIDGET)-$(VERSION).jar com
=======
all : pdf class app

app : Desktop.java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* --module-path $(CLASSPATH) --add-modules javafx.controls Desktop.java

class : java
	javac -Xlint:deprecation -d . -cp .:$(CLASSPATH)/* --module-path $(CLASSPATH) --add-modules javafx.controls $(WIDGET).java
>>>>>>> 30fea70ff88428ba48b47386584487936844d1ae

java : src/$(WIDGET).nw
	notangle -R$(WIDGET).java src/$(WIDGET).nw > $(WIDGET).java

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
pdf : doc/$(WIDGET).tex
=======
pdf : tex
>>>>>>> 774b878179b36b3d27e1da4d86a89ce7d31b587e
=======
pdf : tex
>>>>>>> e4903e6712f100d8acd875fb96c63dff0b780d8e
=======
pdf : tex
>>>>>>> 07460a53ab04f3b98d0f84bd7e8ac3ecea017ba1
=======
pdf : tex
>>>>>>> 30fea70ff88428ba48b47386584487936844d1ae
	pdflatex doc/$(WIDGET).tex

tex : src/$(WIDGET).nw
	noweave -delay -index src/$(WIDGET).nw > doc/$(WIDGET).tex

clean :
	rm -f $(WIDGET).pdf
	rm -f $(WIDGET).java
	rm -rf com/
	latexmk -f -c doc/$(WIDGET).tex
	rm -f doc/$(WIDGET).tex
