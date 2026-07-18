package de.jialiwang.resume.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ApplicationDtos {
    private ApplicationDtos() {}
    public record SaveRequest(
            @NotBlank @Size(max = 250) String jobTitle,
            @Size(max = 250) String companyName,
            @NotBlank @Size(max = 20000) String jobDescription,
            @Size(max = 12000) String candidateSummary) {}
    public record AnalyzeRawRequest(
            @NotBlank @Size(max = 30000) String jobText,
            @Size(max = 12000) String candidateSummary) {}
    public record AnalysisRequest(@NotBlank String analysisJson) {}
    public record SelectionRequest(List<UUID> projectIds) {}
    public record View(UUID id, String jobTitle, String companyName, String jobDescription,
                       String candidateSummary, ApplicationStatus status, String analysisJson,
                       String analysisEditedJson, List<UUID> selectedProjectIds,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        public static View from(JobApplication a) {
            return new View(a.getId(), a.getJobTitle(), a.getCompanyName(), a.getJobDescription(),
                    a.getCandidateSummary(), a.getStatus(), a.getAnalysisJson(), a.getAnalysisEditedJson(),
                    parseIds(a.getSelectedProjectIds()), a.getCreatedAt(), a.getUpdatedAt());
        }
        private static List<UUID> parseIds(String value) {
            if (value == null || value.isBlank()) return List.of();
            return java.util.Arrays.stream(value.split(",")).map(UUID::fromString).toList();
        }
    }
}
