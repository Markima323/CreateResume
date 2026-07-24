package de.jialiwang.resume.resume;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/generations/{generationId}/motivation-letters")
public class MotivationLetterController {
    public record Request(String personalInfo, MotivationLanguage language) {
        public Request { if (language == null) language = MotivationLanguage.DE; }
    }
    public record View(UUID id, MotivationLanguage language, String personalInfo, String subject,
                       String content, OffsetDateTime createdAt) {}
    private final MotivationLetterService service;
    public MotivationLetterController(MotivationLetterService service) { this.service = service; }

    @PostMapping
    View generate(@PathVariable UUID applicationId, @PathVariable UUID generationId, @RequestBody Request request) {
        return view(service.generate(applicationId, generationId, request.language(), request.personalInfo()));
    }
    @GetMapping("/latest")
    ResponseEntity<View> latest(@PathVariable UUID applicationId, @PathVariable UUID generationId) {
        return service.latest(applicationId, generationId).map(letter -> ResponseEntity.ok(view(letter)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
    @GetMapping("/{letterId}/letter.txt")
    ResponseEntity<byte[]> download(@PathVariable UUID applicationId, @PathVariable UUID generationId,
                                    @PathVariable UUID letterId) {
        MotivationLetter letter = service.get(applicationId, generationId, letterId);
        String text = letter.getSubject() + "\n\n" + letter.getContent();
        String name = letter.getLanguage() == MotivationLanguage.DE ? "Motivationsschreiben-Jiali-Wang.txt" : "Cover-Letter-Jiali-Wang.txt";
        return ResponseEntity.ok().contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString())
                .body(text.getBytes(StandardCharsets.UTF_8));
    }
    private View view(MotivationLetter letter) {
        return new View(letter.getId(), letter.getLanguage(), letter.getPersonalInfo(), letter.getSubject(), letter.getContent(), letter.getCreatedAt());
    }
}
