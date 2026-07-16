package de.jialiwang.resume.application;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_application")
public class JobApplication {
    @Id private UUID id;
    @Column(name = "job_title", nullable = false) private String jobTitle;
    @Column(name = "company_name") private String companyName;
    @Column(name = "job_description", nullable = false, columnDefinition = "text") private String jobDescription;
    @Column(name = "candidate_summary", columnDefinition = "text") private String candidateSummary;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApplicationStatus status;
    @Column(name = "analysis_json", columnDefinition = "text") private String analysisJson;
    @Column(name = "analysis_edited_json", columnDefinition = "text") private String analysisEditedJson;
    @Column(name = "selected_project_ids", columnDefinition = "text") private String selectedProjectIds;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected JobApplication() {}

    public JobApplication(String jobTitle, String companyName, String jobDescription, String candidateSummary) {
        this.id = UUID.randomUUID();
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobDescription = jobDescription;
        this.candidateSummary = candidateSummary;
        this.status = ApplicationStatus.DRAFT;
        this.createdAt = this.updatedAt = OffsetDateTime.now();
    }

    public void update(String jobTitle, String companyName, String jobDescription, String candidateSummary) {
        this.jobTitle = jobTitle; this.companyName = companyName; this.jobDescription = jobDescription;
        this.candidateSummary = candidateSummary; this.updatedAt = OffsetDateTime.now();
    }
    public void saveAnalysis(String json) {
        this.analysisJson = json; this.analysisEditedJson = json; this.status = ApplicationStatus.ANALYZED;
        this.updatedAt = OffsetDateTime.now();
    }
    public void editAnalysis(String json) { this.analysisEditedJson = json; this.updatedAt = OffsetDateTime.now(); }
    public void selectProjects(String ids) {
        this.selectedProjectIds = ids; this.status = ApplicationStatus.PROJECTS_SELECTED; this.updatedAt = OffsetDateTime.now();
    }
    public void markContentReady() { this.status = ApplicationStatus.CONTENT_READY; this.updatedAt = OffsetDateTime.now(); }
    public void markGenerated() { this.status = ApplicationStatus.GENERATED; this.updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompanyName() { return companyName; }
    public String getJobDescription() { return jobDescription; }
    public String getCandidateSummary() { return candidateSummary; }
    public ApplicationStatus getStatus() { return status; }
    public String getAnalysisJson() { return analysisJson; }
    public String getAnalysisEditedJson() { return analysisEditedJson; }
    public String getSelectedProjectIds() { return selectedProjectIds; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
