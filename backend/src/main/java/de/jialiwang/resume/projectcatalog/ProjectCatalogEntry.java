package de.jialiwang.resume.projectcatalog;

import java.util.UUID;

public record ProjectCatalogEntry(
        UUID id,
        String slug,
        String nameZh,
        String nameDe,
        String projectType,
        String roleText,
        String summary,
        String technologies,
        String responsibilities,
        String outcomes,
        String facts,
        String keywords,
        boolean sensitive,
        boolean enabled
) {}
