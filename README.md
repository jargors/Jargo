# Jargo - Storage Interface

Created July 2, 2019 | Latest Version 1.0.0 (Nov. 6, 2019)
-------------------- | -----------------------------------

This directory contains the source code for the Jargo storage interface program
and documentation. Both the program code and the LaTeX code are compiled from a
noweb file, `src/Storage.nw` using the noweb commands `notangle` and `noweave`.
See the `Makefile` for details.

For those without notangle and noweave, the "woven" `doc/Storage.tex` and
"tangled" `Storage.java` are provided.


### BUILD PREREQUISITES
*(versions not shown here are unsupported)*

Prerequisites for building the jar (`jargors-Storage-1.0.0.jar`):

- Java(TM) SE Runtime Environment 18.9 (build 11.0.1+13-LTS)
- javac 11.0.1
- jar 11.0.1
- Apache DBCP2 library (`commons-dbcp2-2.7.0.jar`)
- Apache Logging library (`commons-logging-1.2.jar`)
- Apache Pool2 library (`commons-pool2-2.7.0.jar`)

Prerequisites for building the java code (`Storage.java`):

- notangle 2.12

Prerequisites for building the pdf (`Storage.pdf`):

- pdflatex 3.14159265-2.6-1.40.19, included in TeX Live 2018

Prerequisites for building the tex code (`doc/Storage.tex`):

- noweave 2.12


### BUILD INSTRUCTIONS
*(tested on Fedora 27)*

To build the jar (`jargors-Storage-1.0.0.jar`):

- Verify `echo $CLASSPATH` returns a directory that contains
  `commons-dbcp2-2.7.0.jar`, `commons-logging-1.2.jar`, `commons-pool2-2.7.0.jar`
- Verify `javac`, `jar` programs are accessible from command line
- Type `make jar`

To build the java code (`Storage.java`):

- Verify `notangle` program is accessible from command line
- Type `make java`

To build the pdf (`Storage.pdf`):

- Verify `pdflatex` program is accessible from command line
- Type `make pdf`

To build the tex code (`doc/Storage.tex`):

- Verify `noweave` program is accessible from command line
- Type `make tex`

To build all the above using one command:

- Type `make all` or simply `make`

To delete build objects from the directory:

- Type `make clean`

### TESTING THE BUILD

A program is included in `test/correctness` that can be used to test the build.

Prerequisites for running the test program:

- Apache Derby 10.15.1.3

To build and run the test program:

- Verify `echo $DERBYPATH` returns a directory that contains `derby.jar`
- Type `cd test/correctness`
- Type `make class` (if you have successfully built the storage interface jar,
  you should encounter no problems here)
- Type `./run.sh`

