obj=\
	SchedulesTest1.class \
	SchedulesTest2.class

all : $(obj)

SchedulesTest1.class : SchedulesTest1.java
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:../deps:../deps/*:../jar/*:$(JUNIT)/* SchedulesTest1.java

SchedulesTest2.class : SchedulesTest2.java
	javac -Xlint:deprecation -Xlint:unchecked -d . -cp .:../deps:../deps/*:../jar/*:$(JUNIT)/* SchedulesTest2.java
