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

    public String render(List<ProjectLatexParser.ParsedProject> projects, ResumeTailoring tailoring) {
        if (projects.size() != 3 || projects.stream().anyMatch(p -> !p.valid() || p.items().size() != 4)) {
            throw new IllegalArgumentException("必须先确认三个有效项目，每个项目恰好四条内容");
        }
        try {
            String base = template.getContentAsString(StandardCharsets.UTF_8);
            StringBuilder content = new StringBuilder();
            for (ResumeTailoring.ProjectPlan plan : tailoring.projects()) {
                ProjectLatexParser.ParsedProject p = projects.get(plan.sourceIndex());
                content.append("\\resumeProjectHeading\n  {\\textbf{").append(p.title()).append("} $|$ \\emph{")
                        .append(p.technologies()).append("}}\n  {").append(p.context()).append("}\n")
                        .append("\\resumeItemListStart\n");
                plan.itemNumbers().forEach(number -> content.append("  \\resumeItem{")
                        .append(p.items().get(number - 1)).append("}\n"));
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
