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
                List.of(new ResumeTailoring.SkillPlan("devops", ResumeProfile.SKILL_CATEGORIES.get(6).skills()),
                        new ResumeTailoring.SkillPlan("languages", ResumeProfile.SKILL_CATEGORIES.get(0).skills())));

        String rendered = renderer.render(List.of(project("A", 4), project("B", 3), project("C", 2)), tailoring, ResumeVersion.UPWORK);

        assertThat(rendered).contains("\\section{Berufserfahrung}");
        assertThat(rendered).contains("\\section{Projekte}");
        assertThat(rendered).contains("\\section{Studium}");
        assertThat(rendered).contains("\\section{Technische Kenntnisse}");
        assertThat(rendered).contains("\\textbf{A}", "\\textbf{B}", "\\textbf{C}");
        assertThat(rendered).contains("Softwareentwicklerin mit Schwerpunkt IT- und Netzwerktechnik");
        assertThat(rendered.indexOf("\\textbf{A}")).isLessThan(rendered.indexOf("\\textbf{B}"));
        assertThat(rendered.indexOf("\\textbf{DevOps \\& Tools:}")).isLessThan(rendered.indexOf("\\textbf{Programmiersprachen:}"));
        assertThat(rendered).doesNotContain("%%HEADLINE%%", "%%PROJECTS%%", "%%SKILLS%%", "%%CONTACT%%", "markima323@gmail.com", "+49");
    }

    private ProjectLatexParser.ParsedProject project(String title, int count) {
        String items = java.util.stream.IntStream.rangeClosed(1, count).mapToObj(i -> "\\resumeItem{Punkt " + i + "}")
                .collect(java.util.stream.Collectors.joining(" "));
        return parser.parse("\\resumeProjectHeading {\\textbf{" + title + "} $|$ \\emph{Java}} {Einzelentwicklung} "
                + "\\resumeItemListStart "
                + items + " \\resumeItemListEnd", count);
    }
}
