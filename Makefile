MASTER = resume
LATEX = pdflatex
LATEXFLAGS = -interaction=nonstopmode -halt-on-error

.PHONY: all pdf test clean cleanall

all: clean pdf

pdf:
	$(LATEX) $(LATEXFLAGS) $(MASTER).tex
	$(LATEX) $(LATEXFLAGS) $(MASTER).tex

test: clean pdf

clean:
	rm -f *.aux *.bbl *.bcf *.blg *.brf *.fdb_latexmk *.fls *.log *.out *.run.xml *.synctex.gz *.toc
	rm -f *-blx.bib *.*~

cleanall: clean
	rm -f $(MASTER).pdf
