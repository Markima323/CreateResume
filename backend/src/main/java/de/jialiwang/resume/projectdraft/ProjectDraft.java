package de.jialiwang.resume.projectdraft;

import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectcatalog.PortfolioProject;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_draft", uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "position"}))
public class ProjectDraft {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id") private JobApplication application;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id") private PortfolioProject project;
    @Column(nullable = false) private short position;
    @Column(name = "generated_prompt", columnDefinition = "text") private String generatedPrompt;
    @Column(name = "pasted_latex", columnDefinition = "text") private String pastedLatex;
    @Column(name = "parsed_json", columnDefinition = "text") private String parsedJson;
    @Column(name = "validation_errors", columnDefinition = "text") private String validationErrors;
    @Column(nullable = false) private boolean approved;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected ProjectDraft() {}
    public ProjectDraft(JobApplication application, PortfolioProject project, short position, String prompt) {
        this.id = UUID.randomUUID(); this.application = application; this.project = project; this.position = position;
        this.generatedPrompt = prompt; this.updatedAt = OffsetDateTime.now();
    }
    public void replaceProject(PortfolioProject project, String prompt) {
        this.project = project; this.generatedPrompt = prompt; this.pastedLatex = null; this.parsedJson = null;
        this.validationErrors = null; this.approved = false; this.updatedAt = OffsetDateTime.now();
    }
    public void refreshPrompt(String prompt) {
        this.generatedPrompt = prompt;
        this.updatedAt = OffsetDateTime.now();
    }
    public void saveContent(String latex, String parsedJson, String errors, boolean approved) {
        this.pastedLatex = latex; this.parsedJson = parsedJson; this.validationErrors = errors;
        this.approved = approved; this.updatedAt = OffsetDateTime.now();
    }
    public UUID getId() { return id; }
    public UUID getApplicationId() { return application.getId(); }
    public PortfolioProject getProject() { return project; }
    public short getPosition() { return position; }
    public String getGeneratedPrompt() { return generatedPrompt; }
    public String getPastedLatex() { return pastedLatex; }
    public String getParsedJson() { return parsedJson; }
    public String getValidationErrors() { return validationErrors; }
    public boolean isApproved() { return approved; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
