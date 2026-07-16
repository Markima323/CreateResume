package de.jialiwang.resume.projectdraft;

import de.jialiwang.resume.projectcatalog.ProjectDto;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class DraftDtos {
    private DraftDtos() {}
    public record ContentRequest(String latex, boolean approve) {}
    public record View(UUID id, short position, ProjectDto project, String prompt, String latex,
                       ProjectLatexParser.ParsedProject parsed, List<String> errors,
                       boolean approved, OffsetDateTime updatedAt) {}
}
