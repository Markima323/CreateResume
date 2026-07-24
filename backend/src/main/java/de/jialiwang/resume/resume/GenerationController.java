package de.jialiwang.resume.resume;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/generations")
public class GenerationController {
    public record View(UUID id, String status, String errorMessage, OffsetDateTime createdAt,
                       List<String> sourceProjects, ResumeVersion version) {}
    public record GenerateRequest(ResumeVersion version) {
        public GenerateRequest { if (version == null) version = ResumeVersion.WORK; }
    }
    public record ManualRequest(List<String> projects, ResumeVersion version) {
        public ManualRequest { if (version == null) version = ResumeVersion.WORK; }
    }
    private final ResumeGenerationService service;
    public GenerationController(ResumeGenerationService service) { this.service = service; }
    @PostMapping View generate(@PathVariable UUID applicationId, @RequestBody(required = false) GenerateRequest request) {
        return view(service.generate(applicationId, request == null ? ResumeVersion.WORK : request.version()));
    }
    @PostMapping("/manual") View generateManual(@PathVariable UUID applicationId, @RequestBody ManualRequest request) {
        return view(service.generateManual(applicationId, request.projects(), request.version()));
    }
    @GetMapping("/{generationId}") View get(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return view(service.get(applicationId, generationId));
    }
    @GetMapping("/{generationId}/resume.tex") ResponseEntity<Resource> tex(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return download(service.file(applicationId, generationId, false), service.downloadName(applicationId, false), MediaType.TEXT_PLAIN);
    }
    @GetMapping("/{generationId}/resume.pdf") ResponseEntity<Resource> pdf(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return download(service.file(applicationId, generationId, true), service.downloadName(applicationId, true), MediaType.APPLICATION_PDF);
    }
    private ResponseEntity<Resource> download(Resource resource, String name, MediaType type) {
        String disposition = ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString();
        return ResponseEntity.ok().contentType(type).header(HttpHeaders.CONTENT_DISPOSITION, disposition).body(resource);
    }
    private View view(ResumeGeneration generation) {
        return new View(generation.getId(), generation.getStatus(), generation.getErrorMessage(),
                generation.getCreatedAt(), service.sourceProjects(generation), generation.getVersion());
    }
}
