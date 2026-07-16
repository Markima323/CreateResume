package de.jialiwang.resume.projectcatalog;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final PortfolioProjectRepository repository;
    public ProjectController(PortfolioProjectRepository repository) { this.repository = repository; }
    @GetMapping
    List<ProjectDto> list() { return repository.findAllByEnabledTrueOrderByNameZh().stream().map(ProjectDto::from).toList(); }
}
