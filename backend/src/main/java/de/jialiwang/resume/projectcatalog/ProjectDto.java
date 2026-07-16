package de.jialiwang.resume.projectcatalog;

import java.util.UUID;

public record ProjectDto(UUID id, String slug, String nameZh, String nameDe, String projectType,
                         String roleText, String summary, String technologies, String responsibilities,
                         String outcomes, String facts, String keywords) {
    public static ProjectDto from(PortfolioProject p) {
        return new ProjectDto(p.getId(), p.getSlug(), p.getNameZh(), p.getNameDe(), p.getProjectType(),
                p.getRoleText(), p.getSummary(), p.getTechnologies(), p.getResponsibilities(),
                p.getOutcomes(), p.getFacts(), p.getKeywords());
    }
}
