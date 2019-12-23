.PHONY : all clean

obj=\
	DBUpdateServerServiceT1.class \
	DBUpdateServerServiceT2.class

all : $(obj)

$(obj) : %.class : src/%.java
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:../jar/*:$(JUNIT)/* $<

clean :
	rm *.class
