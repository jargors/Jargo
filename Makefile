# Get names of *.java files by looking through src/
JAVA1=$(addsuffix .java, $(subst src/,java/,$(basename $(wildcard src/*.nw))))
JAVA2=$(addsuffix .java, $(subst src/desktop/,java/desktop/,$(basename $(wildcard src/desktop/*.nw))))

VERSION=1.0.0

.PHONY : _mod all src jar pdf purge clean _mod

_mod :
	@printf "_______________________________________________________________\n"
	@printf "Thanks for using Jargo!\n"
	@printf "  Jargo Version: $(VERSION) (January 20, 2020)\n"
	@printf "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
	@printf "Commands:\n"
	@printf "  make all        build library, documentation, and fetch deps\n"
	@printf "  make jar        build library only (jar/jargors-$(VERSION).jar)\n"
	@printf "  make pdf        build documentation only (pdf/jargo.pdf)\n"
	@printf "  make src        build Java sources from Noweb files\n"
	@printf "  make dep        fetch depenencies from the Internet (dep/)\n"
	@printf "  make clean      delete jar/, com/, pdf/, build.log, wget.log\n"
	@printf "  make purge      clean + delete deps, Java sources, doc/body.tex\n"
	@printf "\n"
	@printf "These build commands have been tested on Fedora 27 with:\n"
	@printf "  GNU Make 4.2.1\n"
	@printf "  GNU bash, version 4.4.23(1)-release (x86_64-redhat-linux-gnu)\n"
	@printf "  GNU coreutils 8.27 (cat, echo, printf, mv, rm)\n"
	@printf "  noweb 2.12 (https://github.com/nrnrnr/noweb)\n"
	@printf "  javac 11.0.1\n"
	@printf "  jar 11.0.1\n"
	@printf "  unzip 6.00\n"
	@printf "  wget 1.19.5\n"
	@printf "\n"
	@printf "If you experience any problems, please log an issue at\n"
	@printf "  https://github.com/jargors/Jargo/issues\n"
	@printf "\n"
	@printf "Or to be a contributor, you can fork this repository,\n"
	@printf "make changes in your fork, and submit a pull request\n"
	@printf "  https://github.com/jargors/Jargo/pulls\n"
	@printf "\n"
	@printf "Thank you!\n"
	@printf "===============================================================\n"

all : dep src jar pdf
	@printf "done all\n"

dep :
	@printf "make dep\n"
	@make -s -f makedep.mk
	@printf "done dep\n"

src :
	@printf "make src\n"
	@make -s -f makesrc.mk
	@printf "done src\n"

jar : src
	@printf "make jar\n"
	@make -s -f makejar.mk
	@printf "done jar\n"

pdf : src
	@printf "make pdf\n"
	@make -s -f makepdf.mk
	@printf "done pdf\n"

clean :
	@if [ -d "jar" ]; then printf "remove jar/...\n"; rm -rf jar/ ; fi; \
	 if [ -d "com" ]; then printf "remove com/...\n"; rm -rf com/ ; fi; \
	 if [ -d "pdf" ]; then printf "remove pdf/...\n"; rm -rf pdf/ ; fi; \
	 if [ -s "build.log" ]; then printf "remove build.log...\n"; rm -rf build.log ; fi; \
	 if [ -s "wget.log" ]; then printf "remove wget.log...\n"; rm -rf wget.log ; fi;
	@printf "done clean\n"

purge : clean
	@if [ -d "dep" ]; then printf "purge dep/...\n"; rm -rf dep/ ; fi; \
	 count=`ls -1 java/*.java 2>/dev/null | wc -l`; \
	 if [ $$count != 0 ]; then printf "purge java/ srcs...\n"; rm -f $(JAVA1) $(JAVA2) ; fi; \
	 if [ -f "doc/body.tex" ]; then printf "purge doc/body.tex...\n"; rm -f doc/body.tex ; fi;
	@printf "done purge\n"

