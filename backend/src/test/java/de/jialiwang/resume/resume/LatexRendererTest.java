package de.jialiwang.resume.resume;

import de.jialiwang.resume.projectdraft.ProjectLatexParser;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LatexRendererTest {
    private final ProjectLatexParser parser = new ProjectLatexParser();
    private final LatexRenderer renderer = new LatexRenderer(new ClassPathResource("resume-template/base-resume.tex"));

    @Test
    void keepsCompleteResumeAndReplacesOnlyProjectSection() {
        String rendered = renderer.render(List.of(project("A"), project("B"), project("C")));

        assertThat(rendered).contains("\\section{Berufserfahrung}");
        assertThat(rendered).contains("\\section{Projekte}");
        assertThat(rendered).contains("\\section{Studium}");
        assertThat(rendered).contains("\\section{Technische Kenntnisse}");
        assertThat(rendered).contains("\\textbf{A}", "\\textbf{B}", "\\textbf{C}");
        assertThat(rendered).doesNotContain("%%PROJECTS%%");
    }

    private ProjectLatexParser.ParsedProject project(String title) {
        return parser.parse("\\resumeProjectHeading {\\textbf{" + title + "} $|$ \\emph{Java}} {Einzelentwicklung} "
                + "\\resumeItemListStart "
                + "\\resumeItem{Eins} \\resumeItem{Zwei} \\resumeItem{Drei} \\resumeItem{Vier} "
                + "\\resumeItemListEnd");
    }
}
