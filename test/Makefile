.PHONY : all clean

obj=\
	DBUpdateServerServiceT1.class \
	DBUpdateServerServiceT2.class \
	DBUpdateServerServiceT3.class \
	CacheT1.class \
	CacheT2.class

all : $(obj)

$(obj) : %.class : src/%.java
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:algo/*:../jar/*:$(JUNIT)/* $<

clean :
	rm *.class
