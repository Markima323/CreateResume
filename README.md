# Resume PDF Builder

This project builds `resume.tex` into `resume.pdf` with Docker, so you do not need a local LaTeX installation.

## Build with Docker

Build the local image:

```powershell
docker build --tag resume-builder --file Dockerfile.local .
```

Generate `resume.pdf` from `resume.tex`:

```powershell
docker run --rm --mount type=bind,source="$($PWD.Path)",target=/resume resume-builder
```

## Useful Make Targets

These targets run inside the Docker image. They also work in any Linux/WSL environment that has the required TeX Live packages installed.

```bash
make          # clean temporary files, then build resume.pdf
make pdf      # build resume.pdf without cleaning first
make clean    # remove LaTeX temporary files
make cleanall # remove temporary files and resume.pdf
```

## Project Files

- `resume.tex` is the LaTeX source for the resume.
- `Makefile` contains the PDF build targets.
- `Dockerfile.local` defines the local LaTeX build image.
