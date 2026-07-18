package de.jialiwang.resume.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class AiConfiguration {
    @Bean
    RestClient geminiRestClient(GeminiProperties properties) {
        return RestClient.builder().baseUrl(properties.baseUrl()).build();
    }
}
