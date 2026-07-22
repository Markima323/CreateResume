package de.jialiwang.resume.projectcatalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectCatalogJsonTest {
    @Test
    void containsTwentyTwoIndependentUniqueProjectObjects() throws Exception {
        List<ProjectCatalogEntry> projects = new ObjectMapper().readValue(
                new ClassPathResource("project-catalog/projects.json").getInputStream(), new TypeReference<>() {});

        assertThat(projects).hasSize(22).allSatisfy(project -> {
            assertThat(project.id()).isNotNull();
            assertThat(project.slug()).isNotBlank();
            assertThat(project.nameZh()).isNotBlank();
            assertThat(project.summary()).isNotBlank();
            assertThat(project.facts()).isNotBlank();
            assertThat(project.enabled()).isTrue();
        });
        assertThat(new HashSet<>(projects.stream().map(ProjectCatalogEntry::id).toList())).hasSize(22);
        assertThat(new HashSet<>(projects.stream().map(ProjectCatalogEntry::slug).toList())).hasSize(22);
    }
}
