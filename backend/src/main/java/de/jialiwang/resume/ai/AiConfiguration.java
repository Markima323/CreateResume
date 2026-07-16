package de.jialiwang.resume.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class AiConfiguration {
    @Bean
    RestClient openAiRestClient(OpenAiProperties properties) {
        return RestClient.builder().baseUrl(properties.baseUrl()).build();
    }
}
