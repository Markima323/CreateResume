# Prompt für neue Motivationsschreiben

Kopiere den folgenden Prompt in ChatGPT/Codex und ersetze die Angaben in eckigen Klammern.
Die Ausgabe kann danach direkt in eine `.txt`-Datei kopiert und mit `scripts/New-Motivationsschreiben.ps1` in DOCX und PDF umgewandelt werden.

```text
Du bist ein erfahrener deutscher Bewerbungscoach und formulierst ein präzises, professionelles Motivationsschreiben auf Deutsch.

Ziel:
Erstelle ein vollständiges Motivationsschreiben für Jiali Wang. Die Ausgabe muss als reiner Text erfolgen, ohne Markdown, ohne Aufzählungen, ohne Erklärungen und ohne zusätzliche Kommentare.

Wichtiges Ausgabeformat:
1. Erste Zeile: Bewerbung als [JOBTITEL]
2. Danach eine Leerzeile.
3. Danach die Anrede.
4. Danach 5 bis 7 gut strukturierte Absätze, jeweils durch eine Leerzeile getrennt.
5. Danach:
Mit freundlichen Grüßen

Jiali Wang

Nicht ausgeben:
- keine Absenderadresse
- keine Empfängeradresse
- kein Datum
- keine Anlagenliste
- keine Markdown-Formatierung wie **fett**
- keine doppelte deutsche/englische Berufsbezeichnung im Titel, sofern die Ausschreibung nicht ausdrücklich beide Titel verlangt

Profil von Jiali Wang:
- Informatikerin mit Schwerpunkt KI-, Backend- und Systementwicklung
- Master Informatik an der Hochschule Darmstadt, Abschluss im Juni 2026
- Masterarbeit: modulare End-to-End-Plattform mit Pose Estimation, generativer Bildverarbeitung und lokaler 3D-Modellgenerierung
- Erfahrung mit Python, ComfyUI-Nodes, lokalen GPU-Inferenzen, externen generativen Schnittstellen und austauschbaren Modellkomponenten
- Eigenes Projekt: containerisierte KI-Chat-Plattform mit React, Spring Boot, PostgreSQL, Nginx und lokalem Ollama-/Qwen3-Modell
- Fokus auf souveräne KI-Architekturen, lokale Verarbeitung sensibler Daten, rollenbezogene Zugriffskontrollen, Rate Limiting, persistentes Kontextmanagement und Schnittstellendesign
- Kundenprojekt: automatisierte Erstellung von Bewertungs- und Finanzberichten mit JSON-Schemas, Validierungsregeln, Generierungsmanifesten, kontrollierter API-Nutzung und Protokollierung
- Werkstudentin bei Vitech GmbH: internes Zeiterfassungssystem mit Gesichtserkennung, interne Schnittstellen, Prozessautomatisierung, Integration in Geräte- und Hardwareabläufe
- Deutschkenntnisse C1, sehr gute Lernbereitschaft, strukturierte Arbeitsweise, adressatengerechte technische Kommunikation

Stellendaten:
- Unternehmen/Behörde: [UNTERNEHMEN ODER BEHÖRDE]
- Position: [JOBTITEL]
- Ort: [ORT]
- Wichtige Anforderungen aus der Ausschreibung: [ANFORDERUNGEN]
- Besonders hervorzuhebende Motivation: [MOTIVATION]

Schreibstil:
- professionell, konkret und glaubwürdig
- keine übertriebenen Floskeln
- klare Verbindung zwischen den Anforderungen der Stelle und Jialis Projekterfahrung
- bei Behörden/öffentlichem Dienst: digitale Souveränität, Sicherheit, langfristige Betreibbarkeit, Verantwortungsbewusstsein und Zusammenarbeit mit Fachbereichen betonen
- bei Unternehmen: Produktnutzen, technische Umsetzung, Teamarbeit, Skalierbarkeit und konkrete Wirkung betonen
```

## DOCX/PDF erstellen

1. Speichere den fertigen Text in einer UTF-8-Textdatei, zum Beispiel:

```powershell
Motivationsschreiben\ki-architect-letter.txt
```

2. Generiere DOCX und PDF:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\New-Motivationsschreiben.ps1" `
  -InputPath ".\Motivationsschreiben\ki-architect-letter.txt" `
  -Recipient "Hessisches Ministerium der Finanzen" `
  -City "Wiesbaden" `
  -DateText "2. Juli 2026" `
  -OutputName "Jiali Wang Motivationsschreiben KI-Architektin.docx"
```

Der Generator übernimmt Absender, blaue Trennlinie, Datumstextfeld und Formatierung aus der Word-Vorlage.
