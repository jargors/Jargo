TOPTARGETS := all clean purge

SUBDIRS := $(wildcard */.)

$(TOPTARGETS) : $(SUBDIRS)

$(SUBDIRS):
	make -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)

clean :
	rm -rf *.jar

purge : clean
