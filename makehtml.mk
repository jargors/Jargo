# This file describes how to compile src/*.nw into the Jargo online
# documentation. The output of the make commands are www/jargo.html.
# IMPORTANT: the html target only works if noweb was compiled with icont.

# Build www/jargo.html using 'noweave'. The order of the noweave arguments
# matters, so I use a temporary variable TEXSRCS to establish the order.
TEXSRCS = \
	src/preface.nw \
	src/tut-install.nw \
	src/tut-example.nw \
	src/tut-start.nw \
	src/tut-analyze.nw \
	src/mod-setting.nw \
	src/mod-users.nw \
	src/mod-metrics.nw \
	src/mod-schema.nw \
	src/sim-overview.nw \
	src/sim-administration.nw \
	src/sim-reading.nw \
	src/sim-writing.nw \
	src/sim-gtree.nw \
	src/sim-storage.nw \
	src/sim-controller.nw \
	src/sim-communicator.nw \
	src/sim-client.nw \
	src/sim-traffic.nw \
	src/sim-tools.nw \
	src/ui-overview.nw \
	src/ui-command.nw \
	src/ui-guiapp.nw \
	src/ui-guicontroller.nw \
	src/trouble-debug.nw \
	src/trouble-limitations.nw \
	src/trouble-bugs.nw

bin_noweave:=$(shell command -v noweave 2> /dev/null)

.PHONY : html clean _precheck

# This target produces www/jargo.html.
html : _precheck src/*.nw
	@printf "compile www/jargo.html...\n"
	@printf "weave $@...\n"
	@noweave -filter l2h -index -html $(TEXSRCS) | htmltoc > www/jargo.html

# Remove www/jargo.html
clean :
	@rm -rf www/

# If the user doesn't have noweave, throw and error and stop.
_precheck :
ifndef bin_noweave
	$(error "**ERROR: 'noweave' not found! target 'src' unavailable.")
endif
	@mkdir -p www

