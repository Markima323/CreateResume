package de.jialiwang.resume.application;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService service;
    public ApplicationController(ApplicationService service) { this.service = service; }

    @PostMapping
    ApplicationDtos.View create(@Valid @RequestBody ApplicationDtos.SaveRequest request) {
        return ApplicationDtos.View.from(service.create(request));
    }
    @GetMapping("/{id}")
    ApplicationDtos.View get(@PathVariable UUID id) { return ApplicationDtos.View.from(service.get(id)); }
    @PatchMapping("/{id}")
    ApplicationDtos.View update(@PathVariable UUID id, @Valid @RequestBody ApplicationDtos.SaveRequest request) {
        return ApplicationDtos.View.from(service.update(id, request));
    }
    @PostMapping("/{id}/analyze")
    ApplicationDtos.View analyze(@PathVariable UUID id) { return ApplicationDtos.View.from(service.analyze(id)); }
    @PutMapping("/{id}/analysis")
    ApplicationDtos.View editAnalysis(@PathVariable UUID id, @Valid @RequestBody ApplicationDtos.AnalysisRequest request) {
        return ApplicationDtos.View.from(service.editAnalysis(id, request.analysisJson()));
    }
    @PutMapping("/{id}/selections")
    ApplicationDtos.View select(@PathVariable UUID id, @RequestBody ApplicationDtos.SelectionRequest request) {
        return ApplicationDtos.View.from(service.select(id, request.projectIds()));
    }
}
