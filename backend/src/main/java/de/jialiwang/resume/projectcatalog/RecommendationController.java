package de.jialiwang.resume.projectcatalog;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service) { this.service = service; }
    @PostMapping
    List<RecommendationService.Recommendation> recommend(@PathVariable UUID applicationId) {
        return service.recommend(applicationId);
    }
}
