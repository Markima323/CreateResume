package de.jialiwang.resume.resume;

import de.jialiwang.resume.application.JobApplication;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resume_generation")
public class ResumeGeneration {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id") private JobApplication application;
    @Column(name = "tex_path", nullable = false) private String texPath;
    @Column(name = "pdf_path") private String pdfPath;
    @Column(nullable = false) private String status;
    @Column(name = "error_message", columnDefinition = "text") private String errorMessage;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    protected ResumeGeneration() {}
    public ResumeGeneration(UUID id, JobApplication application, String texPath) {
        this.id = id; this.application = application; this.texPath = texPath;
        this.status = "TEX_READY"; this.createdAt = OffsetDateTime.now();
    }
    public void pdfReady(String pdfPath) { this.pdfPath = pdfPath; this.status = "PDF_READY"; }
    public void failed(String message) { this.status = "TEX_READY"; this.errorMessage = message; }
    public UUID getId() { return id; }
    public UUID getApplicationId() { return application.getId(); }
    JobApplication getApplication() { return application; }
    public String getTexPath() { return texPath; }
    public String getPdfPath() { return pdfPath; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
