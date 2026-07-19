package de.jialiwang.resume.resume;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/generations")
public class GenerationController {
    public record View(UUID id, String status, String errorMessage, OffsetDateTime createdAt) {
        static View from(ResumeGeneration g) { return new View(g.getId(), g.getStatus(), g.getErrorMessage(), g.getCreatedAt()); }
    }
    public record ManualRequest(List<String> projects) {}
    private final ResumeGenerationService service;
    public GenerationController(ResumeGenerationService service) { this.service = service; }
    @PostMapping View generate(@PathVariable UUID applicationId) { return View.from(service.generate(applicationId)); }
    @PostMapping("/manual") View generateManual(@PathVariable UUID applicationId, @RequestBody ManualRequest request) {
        return View.from(service.generateManual(applicationId, request.projects()));
    }
    @GetMapping("/{generationId}") View get(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return View.from(service.get(applicationId, generationId));
    }
    @GetMapping("/{generationId}/resume.tex") ResponseEntity<Resource> tex(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return download(service.file(applicationId, generationId, false), "resume.tex", MediaType.TEXT_PLAIN);
    }
    @GetMapping("/{generationId}/resume.pdf") ResponseEntity<Resource> pdf(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return download(service.file(applicationId, generationId, true), "resume.pdf", MediaType.APPLICATION_PDF);
    }
    private ResponseEntity<Resource> download(Resource resource, String name, MediaType type) {
        return ResponseEntity.ok().contentType(type).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"").body(resource);
    }
}
