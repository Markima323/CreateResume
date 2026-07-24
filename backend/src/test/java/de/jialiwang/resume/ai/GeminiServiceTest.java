package de.jialiwang.resume.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectdraft.ProjectLatexParser;
import de.jialiwang.resume.resume.ResumeProfile;
import de.jialiwang.resume.resume.ResumeTailoring;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

class GeminiServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void extractsJobFieldsAndAnalysisFromInteractionResponse() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1beta");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiService service = new GeminiService(builder.build(),
                new GeminiProperties("test-key", "gemini-3.5-flash", "unused", false, "medium"), mapper);

        ObjectNode output = mapper.createObjectNode()
                .put("jobTitle", "Java Backend Developer")
                .put("companyName", "Beispiel GmbH")
                .put("jobDescription", "Entwicklung von Spring-Boot-Anwendungen");
        ObjectNode analysis = mapper.createObjectNode();
        analysis.putArray("plainLanguageDuties").add("Backend entwickeln");
        output.set("analysis", analysis);
        ObjectNode textContent = mapper.createObjectNode().put("type", "text").put("text", mapper.writeValueAsString(output));
        ObjectNode modelOutput = mapper.createObjectNode().put("type", "model_output");
        modelOutput.putArray("content").add(textContent);
        ObjectNode response = mapper.createObjectNode().put("status", "completed");
        response.putArray("steps").add(modelOutput);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/interactions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(jsonPath("$.model").value("gemini-3.5-flash"))
                .andExpect(jsonPath("$.store").value(false))
                .andExpect(jsonPath("$.generation_config.thinking_level").value("medium"))
                .andExpect(jsonPath("$.response_format.mime_type").value("application/json"))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        GeminiService.ExtractedJob result = service.extractAndAnalyzeJob("Stellenanzeige");

        assertThat(result.jobTitle()).isEqualTo("Java Backend Developer");
        assertThat(result.companyName()).isEqualTo("Beispiel GmbH");
        assertThat(result.jobDescription()).contains("Spring-Boot");
        assertThat(mapper.readTree(result.analysisJson()).path("plainLanguageDuties").get(0).asText())
                .isEqualTo("Backend entwickeln");
        server.verify();
    }

    @Test
    void rejectsCallsWithoutApiKey() {
        GeminiService service = new GeminiService(RestClient.create(),
                new GeminiProperties("", "gemini-3.5-flash", "unused", false, "medium"), mapper);

        assertThatThrownBy(() -> service.extractAndAnalyzeJob("Stellenanzeige"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }

    @Test
    void createsFinalJobSpecificTailoringWithoutChangingFacts() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1beta");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiService service = new GeminiService(builder.build(),
                new GeminiProperties("test-key", "gemini-3.5-flash", "unused", false, "medium"), mapper);

        ObjectNode output = mapper.createObjectNode()
                .put("headline", "Softwareentwicklerin mit Schwerpunkt IT- und Netzwerktechnik");
        var skillGroups = output.putArray("skillGroups");
        for (int index : List.of(6, 0)) {
            ResumeProfile.SkillCategory category = ResumeProfile.SKILL_CATEGORIES.get(index);
            ObjectNode group = mapper.createObjectNode().put("categoryId", category.id());
            group.set("skills", mapper.valueToTree(category.skills()));
            skillGroups.add(group);
        }
        ObjectNode modelOutput = mapper.createObjectNode().put("type", "model_output");
        modelOutput.putArray("content").add(mapper.createObjectNode().put("type", "text")
                .put("text", mapper.writeValueAsString(output)));
        ObjectNode response = mapper.createObjectNode();
        response.putArray("steps").add(modelOutput);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/interactions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("Netzwerktechnik")))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        JobApplication application = new JobApplication("IT- und Netzwerktechnik", "", "Netzwerktechnik", "");
        ProjectLatexParser parser = new ProjectLatexParser();
        List<ProjectLatexParser.ParsedProject> projects = List.of(parsed(parser, "A"), parsed(parser, "B"), parsed(parser, "C"));
        ResumeTailoring result = service.tailorResume(application, projects);

        assertThat(result.headline()).endsWith("IT- und Netzwerktechnik");
        assertThat(result.skillGroups().get(0).categoryId()).isEqualTo("devops");
        assertThat(result.skillGroups()).hasSize(2);
        server.verify();
    }

    private ProjectLatexParser.ParsedProject parsed(ProjectLatexParser parser, String title) {
        return parser.parse("\\resumeProjectHeading {\\textbf{" + title + "} $|$ \\emph{Java}} {Einzelentwicklung} "
                + "\\resumeItemListStart \\resumeItem{Eins} \\resumeItem{Zwei} "
                + "\\resumeItem{Drei} \\resumeItem{Vier} \\resumeItemListEnd");
    }
}
