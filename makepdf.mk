.PHONY : pdf clean

# Compile jargo.pdf from jargo.tex
pdf : jargo.tex doc/body.tex
	texfot pdflatex -halt-on-error jargo.tex

clean :
	rm -f jargo.pdf
	latexmk -f -c jargo.tex
