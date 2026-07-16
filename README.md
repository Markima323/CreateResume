# Resume PDF Builder

## Semi-automatic Resume Generator

The repository now includes a Web application for this workflow:

1. Paste a German job description.
2. Generate a structured Chinese job analysis with the OpenAI Responses API.
3. Recommend and confirm three matching portfolio projects.
4. Copy three generated prompts into separate Codex windows.
5. Paste the German LaTeX project descriptions back into the application.
6. Validate exactly four resume items per project and generate TeX/PDF.

The implementation uses Java 21, Spring Boot 3, React, PostgreSQL, Docker Compose and Nginx. See [the development document](docs/开发文档.md) for architecture and design decisions.

### Run the application

Create the local environment file:

```powershell
Copy-Item .env.example .env
```

Set `OPENAI_API_KEY` in `.env`, then build and start all services:

```powershell
docker compose up -d --build
```

Open:

```text
http://localhost:8088
```

Check service status or stop the application:

```powershell
docker compose ps
docker compose down
```

PostgreSQL and generated files use named Docker volumes, so `docker compose down` does not delete them. Use `docker compose down -v` only when you intentionally want to delete local application data.

### Development checks

The project includes Docker-backed Maven launchers because a global Maven installation is not required:

```powershell
cd backend
.\mvnw.cmd test
```

Build the frontend without changing the PowerShell execution policy:

```powershell
cd frontend
npm.cmd install
npm.cmd run build
```

Without `OPENAI_API_KEY`, the application and local project catalog still start, but the job-analysis endpoint returns a clear configuration error. Project recommendation has a deterministic local fallback if the AI reranking request is temporarily unavailable.

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

## Motivationsschreiben

The Word template lives at:

```powershell
Motivationsschreiben\Jiali Wang anschreiben.docx
```

Write or paste the finished letter text into a UTF-8 `.txt` file. The first line should be the subject, followed by the salutation, body paragraphs, closing and signature. See:

```powershell
Motivationsschreiben\ki-architect-letter.txt
Motivationsschreiben\PROMPT.md
```

Generate DOCX and PDF:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\New-Motivationsschreiben.ps1" `
  -InputPath ".\Motivationsschreiben\ki-architect-letter.txt" `
  -Recipient "Hessisches Ministerium der Finanzen" `
  -City "Wiesbaden" `
  -DateText "2. Juli 2026" `
  -OutputName "Jiali Wang Motivationsschreiben KI-Architektin.docx"
```

PDF export uses Microsoft Word in PDF/A mode if available, then falls back to LibreOffice.
