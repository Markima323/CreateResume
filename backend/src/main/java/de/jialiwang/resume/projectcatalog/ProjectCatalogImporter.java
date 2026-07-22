package de.jialiwang.resume.projectcatalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProjectCatalogImporter implements ApplicationRunner {
    private static final int EXPECTED_PROJECT_COUNT = 22;

    private final Resource catalog;
    private final ObjectMapper mapper;
    private final PortfolioProjectRepository projects;

    public ProjectCatalogImporter(@Value("classpath:project-catalog/projects.json") Resource catalog,
                                  ObjectMapper mapper, PortfolioProjectRepository projects) {
        this.catalog = catalog;
        this.mapper = mapper;
        this.projects = projects;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<ProjectCatalogEntry> entries = mapper.readValue(catalog.getInputStream(), new TypeReference<>() {});
        validate(entries);
        Map<UUID, PortfolioProject> existing = projects.findAll().stream()
                .collect(Collectors.toMap(PortfolioProject::getId, Function.identity()));
        Set<UUID> catalogIds = entries.stream().map(ProjectCatalogEntry::id).collect(Collectors.toSet());
        List<PortfolioProject> synchronizedProjects = entries.stream()
                .map(entry -> {
                    PortfolioProject project = existing.getOrDefault(entry.id(), PortfolioProject.from(entry));
                    project.synchronize(entry);
                    return project;
                }).toList();
        existing.values().stream().filter(project -> !catalogIds.contains(project.getId()))
                .forEach(PortfolioProject::disable);
        projects.saveAll(synchronizedProjects);
    }

    private void validate(List<ProjectCatalogEntry> entries) {
        if (entries.size() != EXPECTED_PROJECT_COUNT) {
            throw new IllegalStateException("项目 JSON 必须恰好包含 22 条，当前为 " + entries.size());
        }
        Set<UUID> ids = new HashSet<>();
        Set<String> slugs = new HashSet<>();
        for (ProjectCatalogEntry entry : entries) {
            if (entry.id() == null || blank(entry.slug()) || blank(entry.nameZh()) || blank(entry.nameDe())
                    || blank(entry.projectType()) || blank(entry.roleText()) || blank(entry.summary())
                    || blank(entry.technologies()) || blank(entry.responsibilities()) || blank(entry.facts())
                    || blank(entry.keywords())) {
                throw new IllegalStateException("项目 JSON 存在空的必填字段：" + entry.slug());
            }
            if (!ids.add(entry.id()) || !slugs.add(entry.slug())) {
                throw new IllegalStateException("项目 JSON 存在重复 ID 或 slug：" + entry.slug());
            }
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
