package de.jialiwang.resume.projectcatalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jialiwang.resume.ai.AiUnavailableException;
import de.jialiwang.resume.ai.OpenAiService;
import de.jialiwang.resume.application.ApplicationService;
import de.jialiwang.resume.application.JobApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RecommendationService {
    public record Recommendation(ProjectDto project, int score, List<String> matchedKeywords, String reason,
                                 List<String> gaps, String source) {}
    private final PortfolioProjectRepository projects;
    private final ApplicationService applications;
    private final OpenAiService ai;
    private final ObjectMapper mapper;

    public RecommendationService(PortfolioProjectRepository projects, ApplicationService applications,
                                 OpenAiService ai, ObjectMapper mapper) {
        this.projects = projects; this.applications = applications; this.ai = ai; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Recommendation> recommend(UUID applicationId) {
        JobApplication application = applications.get(applicationId);
        String analysis = application.getAnalysisEditedJson();
        if (analysis == null || analysis.isBlank()) throw new IllegalArgumentException("请先完成岗位分析");
        List<Scored> local = localRank(application, projects.findAllByEnabledTrueOrderByNameZh());
        List<PortfolioProject> candidates = local.stream().limit(10).map(Scored::project).toList();
        try {
            List<Recommendation> aiResults = parseAi(ai.rankProjects(analysis, candidates), candidates);
            if (aiResults.size() == 3) return aiResults;
        } catch (AiUnavailableException | IllegalArgumentException ignored) {
            // A deterministic recommendation remains available when the provider is temporarily unavailable.
        }
        return local.stream().limit(3).map(s -> new Recommendation(ProjectDto.from(s.project()), s.score(),
                s.matches(), "根据岗位文本与项目技术、职责关键词的本地匹配结果", List.of(), "LOCAL_FALLBACK")).toList();
    }

    private List<Scored> localRank(JobApplication a, List<PortfolioProject> all) {
        Set<String> tokens = tokenize(a.getJobTitle() + " " + a.getJobDescription() + " " + a.getAnalysisEditedJson());
        return all.stream().map(p -> {
            Set<String> projectTokens = tokenize(p.getTechnologies() + " " + p.getResponsibilities() + " " + p.getKeywords());
            List<String> matches = projectTokens.stream().filter(tokens::contains).sorted().limit(12).toList();
            int score = Math.min(95, 25 + matches.size() * 7);
            return new Scored(p, score, matches);
        }).sorted(Comparator.comparingInt(Scored::score).reversed().thenComparing(x -> x.project().getNameZh())).toList();
    }

    private List<Recommendation> parseAi(String json, List<PortfolioProject> candidates) {
        try {
            Map<UUID, PortfolioProject> allowed = new HashMap<>();
            candidates.forEach(p -> allowed.put(p.getId(), p));
            List<Recommendation> result = new ArrayList<>();
            Set<UUID> seen = new HashSet<>();
            for (JsonNode n : mapper.readTree(json).path("recommendations")) {
                UUID id = UUID.fromString(n.path("projectId").asText());
                PortfolioProject p = allowed.get(id);
                if (p == null || !seen.add(id)) throw new IllegalArgumentException("AI 返回了无效项目");
                result.add(new Recommendation(ProjectDto.from(p), n.path("score").asInt(), strings(n.path("matchedKeywords")),
                        n.path("reason").asText(), strings(n.path("gaps")), "OPENAI"));
            }
            return result;
        } catch (Exception e) { throw new IllegalArgumentException("无法解析 AI 推荐结果", e); }
    }

    private List<String> strings(JsonNode array) { List<String> r = new ArrayList<>(); array.forEach(n -> r.add(n.asText())); return r; }
    private Set<String> tokenize(String input) {
        Set<String> out = new HashSet<>();
        for (String token : Pattern.compile("[^\\p{L}\\p{N}+#.]+", Pattern.UNICODE_CHARACTER_CLASS).split(input.toLowerCase(Locale.ROOT))) {
            if (token.length() > 1) out.add(token);
        }
        return out;
    }
    private record Scored(PortfolioProject project, int score, List<String> matches) {}
}
