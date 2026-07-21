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
    void tailorsHeadlineProjectOrderItemCountsAndSkills() {
        ResumeTailoring tailoring = new ResumeTailoring(
                "Softwareentwicklerin mit Schwerpunkt IT- und Netzwerktechnik",
                List.of(new ResumeTailoring.ProjectPlan(2, List.of(1, 2, 3, 4)),
                        new ResumeTailoring.ProjectPlan(0, List.of(1, 3)),
                        new ResumeTailoring.ProjectPlan(1, List.of(2, 3, 4))),
                List.of(new ResumeTailoring.SkillPlan("devops", ResumeProfile.SKILL_CATEGORIES.get(6).skills()),
                        new ResumeTailoring.SkillPlan("languages", ResumeProfile.SKILL_CATEGORIES.get(0).skills())));

        String rendered = renderer.render(List.of(project("A"), project("B"), project("C")), tailoring);

        assertThat(rendered).contains("\\section{Berufserfahrung}");
        assertThat(rendered).contains("\\section{Projekte}");
        assertThat(rendered).contains("\\section{Studium}");
        assertThat(rendered).contains("\\section{Technische Kenntnisse}");
        assertThat(rendered).contains("\\textbf{A}", "\\textbf{B}", "\\textbf{C}");
        assertThat(rendered).contains("Softwareentwicklerin mit Schwerpunkt IT- und Netzwerktechnik");
        assertThat(rendered.indexOf("\\textbf{C}")).isLessThan(rendered.indexOf("\\textbf{A}"));
        assertThat(rendered.indexOf("\\textbf{DevOps \\& Tools:}")).isLessThan(rendered.indexOf("\\textbf{Programmiersprachen:}"));
        assertThat(rendered).doesNotContain("%%HEADLINE%%", "%%PROJECTS%%", "%%SKILLS%%");
    }

    private ProjectLatexParser.ParsedProject project(String title) {
        return parser.parse("\\resumeProjectHeading {\\textbf{" + title + "} $|$ \\emph{Java}} {Einzelentwicklung} "
                + "\\resumeItemListStart "
                + "\\resumeItem{Eins} \\resumeItem{Zwei} \\resumeItem{Drei} \\resumeItem{Vier} "
                + "\\resumeItemListEnd");
    }
}
