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

    public String render(List<ProjectLatexParser.ParsedProject> projects) {
        if (projects.size() != 3 || projects.stream().anyMatch(p -> !p.valid() || p.items().size() != 4)) {
            throw new IllegalArgumentException("必须先确认三个有效项目，每个项目恰好四条内容");
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
            return base.replace("%%PROJECTS%%", content.toString());
        } catch (Exception e) { throw new IllegalStateException("无法读取 LaTeX 模板", e); }
    }
}
