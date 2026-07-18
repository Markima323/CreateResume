package de.jialiwang.resume.ai;

import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        boolean store,
        @Pattern(regexp = "minimal|low|medium|high") String thinkingLevel) {}
