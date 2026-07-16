package de.jialiwang.resume.projectdraft;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectLatexParserTest {
    private final ProjectLatexParser parser = new ProjectLatexParser();

    @Test
    void parsesExactlyFourItems() {
        String latex = """
                \\resumeProjectHeading
                  {\\textbf{ZeitPlan} $|$ \\emph{Java, React, PostgreSQL}}
                  {Persönliches Projekt / Einzelentwicklung}
                \\resumeItemListStart
                  \\resumeItem{Konzeption einer Anwendung}
                  \\resumeItem{Implementierung des Backends}
                  \\resumeItem{Entwicklung der Oberfläche}
                  \\resumeItem{Docker-basierte Bereitstellung}
                \\resumeItemListEnd
                """;
        var result = parser.parse(latex);
        assertThat(result.errors()).isEmpty();
        assertThat(result.title()).isEqualTo("ZeitPlan");
        assertThat(result.context()).isEqualTo("Persönliches Projekt / Einzelentwicklung");
        assertThat(result.items()).hasSize(4);
    }

    @Test
    void rejectsDangerousCommandsAndWrongItemCount() {
        var result = parser.parse("\\input{secret} \\resumeItem{one}");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(x -> x.contains("禁止"));
        assertThat(result.errors()).anyMatch(x -> x.contains("4 个"));
    }
}
