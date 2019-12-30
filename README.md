# Jargo

![](https://github.com/jargors/jargo/workflows/Build/badge.svg) (**master**)

Latest Version: 1.0.0 (Dec. 30, 2019) (in-development)

Jargo is the second incarnation of [Cargo](https://github.com/jamjpan/Cargo), a
real-time ridesharing simulator to help researchers evaluate the quality of
ridesharing algorithms. Jargo adds support for configurable traffic effects,
flexible vehicle routing, and over 20 quality metrics. Ridesharing algorithms
and traffic functions are modular and can be easily changed and combined.
Quality metrics that are not provided may be obtainable by submitting custom
queries against Jargo's data model.

Jargo is written in the [literate programming](http://literateprogramming.com/)
style using [Noweb](https://www.cs.tufts.edu/~nr/noweb/). The idea behind LP is
to write code that can be understood by humans as well as machines, or in other
words code that is explainable. The end result is that the same code can be
compiled into a PDF intended for humans using the command `make pdf`, into Java
code intended for the Java compiler using the command `make src`, and into
bytecode intended for the Java virtual machine using the command `make jar`.

## Quick Start: Command-line (todo: platforms?)

If your machine can access the internet and has `bash` and the `mkdir`, `curl`,
`unzip`, and `tar` command-line programs, run `./getdeps.sh` once to get all
the build prerequisites into a directory called `deps/` (fetch size 52MB,
extracted size 108MB). Then run `make jar` to produce the bytecode packaged
into `jar/jargors-1.0.0.jar`.

### Example

Make a new directory with the following contents:

```
mydir/
  | jargors-1.0.0.jar
  | libgtree.so
  | chengdu.instance
  | chengdu.rnet
  | chengdu.gtree
  | app.java
```

Obtain `libgtree.so` from (todo) and obtain the `chengdu.*` files from (todo).
Add the following code to `app.java`:

```
import com.github.jargors.*;
class app {
  public static void main(String args[]) throws Exception {
    Controller controller = new Controller();
    controller.createNewInstance();
    controller.loadDataModel();
    controller.loadGtree("chengdu.gtree");
    controller.loadRoadNetworkFromFile("chengdu.rnet");
    controller.loadProblem("chengdu.instance");
    System.out.printf("#vertices=%d\n", controller.queryCountVertices()[0]);
    System.out.printf("#edges=%d\n", controller.queryCountEdges()[0]);
    System.out.printf("#servers=%d\n", controller.queryCountServers()[0]);
    System.out.printf("#requests=%d\n", controller.queryCountRequests()[0]);
    controller.closeInstance();
  }
}
```

Type `javac -cp jargors-1.0.0.jar app.java` to produce `app.class`. Now set
the `DERBYPATH` environmental variable to point to a local installation of
[Apache Derby](https://db.apache.org/derby/) by typing
`export DERBYPATH=(path to derby.jar)`. Now type
`java -cp .:jargors-1.0.0.jar:$DERBYPATH/* app`. After a minute or so, you
should get the following output:

```
#vertices=33609
#edges=73832
#servers=5000
#requests=8922
```

## Quick Start: IntelliJ (todo)

## Quick Start: Eclipse (todo)

## Using the GUI (todo)

The graphical Jargo Desktop can be started using `gui-launcher.sh`.

Prerequisites for running Desktop:

- JavaFX 13+14
- Apache Derby 10.15.1.3
- Apache DBCP2 library (`commons-dbcp2-2.7.0.jar`)
- Apache Logging library (`commons-logging-1.2.jar`)
- Apache Pool2 library (`commons-pool2-2.7.0.jar`)
- `jargors-1.0.0.jar`
- `jargors-Exceptions-1.0.0.jar`
- `jargors-GTreeJNI-1.0.0.jar`
- `libgtree.so`

To start Desktop:

- Verify directory `jar` contains `jargors-1.0.0.jar`.
- Verify `echo $LD_LIBRARY_PATH` returns a directory that contains `libgtree.so`.
- Verify `echo $DERBYPATH` returns a directory that contains `derby.jar`.
- Verify `echo $CLASSPATH` returns a directory that contains
  `jargors-GTreeJNI-1.0.0.jar`, `jargors-Exceptions-1.0.0.jar`
- Verify `echo $CLASSPATH` returns a directory that contains
  `commons-dbcp2-2.7.0.jar`, `commons-logging-1.2.jar`, `commons-pool2-2.7.0.jar`
- Verify `echo $CLASSPATH` returns a directory that contains
  JavaFX libraries `javafx.base.jar`, `javafx.controls.jar`, etc.
- Type `./gui-launcher.sh`

## Detailed Build Guide (todo)

### BUILD PREREQUISITES
*(versions not shown here are unsupported)*

Building requires the GNU Make 4.2.1 program and several prerequisites.

Prerequisites for building the jar (`jargors-1.0.0.jar`):

- Java(TM) SE Runtime Environment 18.9 (build 11.0.1+13-LTS)
- JavaFX 13
- javac 11.0.1
- jar 11.0.1
- Apache DBCP2 library (`commons-dbcp2-2.7.0.jar`)
- Apache Logging library (`commons-logging-1.2.jar`)
- Apache Pool2 library (`commons-pool2-2.7.0.jar`)
- `jargors-Exceptions-1.0.0.jar`
- `jargors-GTreeJNI-1.0.0.jar`

Prerequisites for building the java codes (`java/*.java`):

- notangle 2.12

Prerequisites for building the tex codes (`doc/*.tex`):

- noweave 2.12

Prerequisites for building the pdf (`jargo.pdf`):

- pdflatex 3.14159265-2.6-1.40.19, included in TeX Live 2018
- texfot 1.37, included in TeX Live 2018


### BUILD INSTRUCTIONS
*(tested on Fedora 27)*

To build the jar (`jargors-1.0.0.jar`):

- Verify `echo $CLASSPATH` returns a directory that contains
  `jargors-GTreeJNI-1.0.0.jar`, `jargors-Exceptions-1.0.0.jar`
- Verify `echo $CLASSPATH` returns a directory that contains
  `commons-dbcp2-2.7.0.jar`, `commons-logging-1.2.jar`, `commons-pool2-2.7.0.jar`
- Verify `echo $CLASSPATH` returns a directory that contains
  JavaFX libraries `javafx.base.jar`, `javafx.controls.jar`, etc.
- Verify `javac`, `jar` programs are accessible from command line
- Type `make jar`

To build the java codes (`java/*.java`):

- Verify `notangle` program is accessible from command line
- Type `make java`

To build the pdf (`jargo.pdf`):

- Verify `pdflatex` and `texfot` programs are accessible from command line
- Type `make pdf`

To build the tex codes (`doc/*.tex`):

- Verify `noweave` program is accessible from command line
- Type `make tex`

To build all the above using one command:

- Type `make all` or simply `make`

To delete build objects from the directory:

- Type `make clean`

To delete the java and tex codes in addition to the build objects:

- Type `make purge`

