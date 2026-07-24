package de.jialiwang.resume.resume;

import de.jialiwang.resume.application.JobApplication;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "motivation_letter")
public class MotivationLetter {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id") private JobApplication application;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "generation_id") private ResumeGeneration generation;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MotivationLanguage language;
    @Column(name = "personal_info", columnDefinition = "text") private String personalInfo;
    @Column(nullable = false, columnDefinition = "text") private String subject;
    @Column(nullable = false, columnDefinition = "text") private String content;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    protected MotivationLetter() {}
    public MotivationLetter(JobApplication application, ResumeGeneration generation, MotivationLanguage language,
                            String personalInfo, String subject, String content) {
        this.id = UUID.randomUUID(); this.application = application; this.generation = generation;
        this.language = language; this.personalInfo = personalInfo; this.subject = subject; this.content = content;
        this.createdAt = OffsetDateTime.now();
    }
    public UUID getId() { return id; }
    public UUID getApplicationId() { return application.getId(); }
    public UUID getGenerationId() { return generation.getId(); }
    public MotivationLanguage getLanguage() { return language; }
    public String getPersonalInfo() { return personalInfo; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
