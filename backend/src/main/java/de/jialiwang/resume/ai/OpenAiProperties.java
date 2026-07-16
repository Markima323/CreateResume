package de.jialiwang.resume.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAiProperties(String apiKey, String model, String baseUrl, boolean store) {}
