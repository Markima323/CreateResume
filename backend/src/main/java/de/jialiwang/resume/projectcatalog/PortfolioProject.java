package de.jialiwang.resume.projectcatalog;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_project")
public class PortfolioProject {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String slug;
    @Column(name = "name_zh", nullable = false) private String nameZh;
    @Column(name = "name_de") private String nameDe;
    @Column(name = "project_type", nullable = false) private String projectType;
    @Column(name = "role_text", nullable = false) private String roleText;
    @Column(nullable = false, columnDefinition = "text") private String summary;
    @Column(nullable = false, columnDefinition = "text") private String technologies;
    @Column(nullable = false, columnDefinition = "text") private String responsibilities;
    @Column(nullable = false, columnDefinition = "text") private String outcomes;
    @Column(nullable = false, columnDefinition = "text") private String facts;
    @Column(nullable = false, columnDefinition = "text") private String keywords;
    @Column(nullable = false) private boolean sensitive;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected PortfolioProject() {}
    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getNameZh() { return nameZh; }
    public String getNameDe() { return nameDe; }
    public String getProjectType() { return projectType; }
    public String getRoleText() { return roleText; }
    public String getSummary() { return summary; }
    public String getTechnologies() { return technologies; }
    public String getResponsibilities() { return responsibilities; }
    public String getOutcomes() { return outcomes; }
    public String getFacts() { return facts; }
    public String getKeywords() { return keywords; }
    public boolean isSensitive() { return sensitive; }
    public boolean isEnabled() { return enabled; }
}
