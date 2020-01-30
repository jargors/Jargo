.PHONY : pdf clean _prep

################################################################################
# http://www.lunderberg.com/2015/08/25/cpp-makefile-pretty-output/
define run
$(1) >> pdf/build.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat pdf/build.log; \
fi; \
exit $$RES
endef
################################################################################

# Compile jargo.pdf from jargo.tex
pdf : _prep doc/jargo.tex doc/body.tex
	@mkdir -p pdf
	@printf "compile jargo.pdf...\n"
	@$(call run, texfot pdflatex -output-directory pdf -halt-on-error doc/jargo.tex)

clean :
	@rm -f jargo.pdf
	@latexmk -f -c doc/jargo.tex

_prep :
	@rm -rf pdf/build.log

