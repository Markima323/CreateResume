package de.jialiwang.resume.projectdraft;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/drafts")
public class DraftController {
    private final DraftService service;
    public DraftController(DraftService service) { this.service = service; }
    @GetMapping List<DraftDtos.View> list(@PathVariable UUID applicationId) { return service.views(applicationId); }
    @PostMapping("/prompts") List<DraftDtos.View> prompts(@PathVariable UUID applicationId) { return service.initialize(applicationId); }
    @PutMapping("/{position}") DraftDtos.View save(@PathVariable UUID applicationId, @PathVariable short position,
                                                   @RequestBody DraftDtos.ContentRequest request) {
        return service.save(applicationId, position, request);
    }
}
