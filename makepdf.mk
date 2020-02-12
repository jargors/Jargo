# This file describes how to compile doc/jargo.tex into the Jargo
# documentation.  The output of the make commands are pdf/jargo.pdf, along with
# the LaTeX build objects.

.PHONY : pdf clean _prep

################################################################################
# See 'makejar.mk' for a description of this function. Source:
#     http://www.lunderberg.com/2015/08/25/cpp-makefile-pretty-output/
define run
$(1) >> pdf/build.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat pdf/build.log; \
fi; \
exit $$RES
endef
################################################################################

# This target produces pdf/jargo.pdf.
pdf : _prep doc/jargo.tex doc/body.tex
	@mkdir -p pdf
	@printf "compile jargo.pdf...\n"
	@$(call run, texfot pdflatex -output-directory pdf -halt-on-error doc/jargo.tex)

# Remove pdf/jargo.pdf and LaTeX build objects.
clean :
	@rm -f jargo.pdf
	@latexmk -f -c doc/jargo.tex

# Remove any existing build.log.
_prep :
	@rm -rf pdf/build.log

