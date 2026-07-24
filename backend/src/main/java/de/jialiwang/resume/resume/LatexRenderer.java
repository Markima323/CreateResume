package de.jialiwang.resume.resume;

import de.jialiwang.resume.projectdraft.ProjectLatexParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class LatexRenderer {
    private final Resource template;
    public LatexRenderer(@Value("${app.latex-template}") Resource template) { this.template = template; }

    public String render(List<ProjectLatexParser.ParsedProject> projects, ResumeTailoring tailoring, ResumeVersion version) {
        if (projects.size() != 3 || java.util.stream.IntStream.range(0, 3)
                .anyMatch(index -> !projects.get(index).valid() || projects.get(index).items().size() != 4 - index)) {
            throw new IllegalArgumentException("必须先确认三个有效项目，项目内容依次为 4、3、2 条");
        }
        try {
            String base = template.getContentAsString(StandardCharsets.UTF_8);
            StringBuilder content = new StringBuilder();
            for (ProjectLatexParser.ParsedProject p : projects) {
                content.append("\\resumeProjectHeading\n  {\\textbf{").append(p.title()).append("} $|$ \\emph{")
                        .append(p.technologies()).append("}}\n  {").append(p.context()).append("}\n")
                        .append("\\resumeItemListStart\n");
                p.items().forEach(item -> content.append("  \\resumeItem{").append(item).append("}\n"));
                content.append("\\resumeItemListEnd\n");
            }
            StringBuilder skillContent = new StringBuilder();
            for (ResumeTailoring.SkillPlan plan : tailoring.skillGroups()) {
                ResumeProfile.SkillCategory category = ResumeProfile.SKILL_CATEGORIES.stream()
                        .filter(candidate -> candidate.id().equals(plan.categoryId())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("未知技能分类：" + plan.categoryId()));
                skillContent.append("    \\textbf{").append(escape(category.label())).append(":} ")
                        .append(plan.skills().stream().map(this::escape).collect(java.util.stream.Collectors.joining(", ")))
                        .append(" \\\\\n");
            }
            return base.replace("%%HEADLINE%%", escape(tailoring.headline()))
                    .replace("%%CONTACT%%", version == ResumeVersion.UPWORK
                            ? "\\href{https://github.com/Markima323}{\\underline{github.com/Markima323}}"
                            : "+49 151 5578 5518 $|$\n  \\href{mailto:markima323@gmail.com}{\\underline{markima323@gmail.com}} $|$\n  \\href{https://github.com/Markima323}{\\underline{github.com/Markima323}}")
                    .replace("%%PROJECTS%%", content.toString())
                    .replace("%%SKILLS%%", skillContent.toString());
        } catch (Exception e) { throw new IllegalStateException("无法读取 LaTeX 模板", e); }
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char ch : value.toCharArray()) {
            escaped.append(switch (ch) {
                case '\\' -> "\\textbackslash{}";
                case '{' -> "\\{";
                case '}' -> "\\}";
                case '&' -> "\\&";
                case '%' -> "\\%";
                case '$' -> "\\$";
                case '#' -> "\\#";
                case '_' -> "\\_";
                case '~' -> "\\textasciitilde{}";
                case '^' -> "\\textasciicircum{}";
                default -> String.valueOf(ch);
            });
        }
        return escaped.toString();
    }
}
