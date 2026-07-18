package de.jialiwang.resume.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jialiwang.resume.ai.GeminiService;
import de.jialiwang.resume.common.NotFoundException;
import de.jialiwang.resume.projectcatalog.PortfolioProject;
import de.jialiwang.resume.projectcatalog.PortfolioProjectRepository;
import de.jialiwang.resume.projectdraft.ProjectDraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private final JobApplicationRepository applications;
    private final PortfolioProjectRepository projects;
    private final ProjectDraftRepository drafts;
    private final GeminiService ai;
    private final ObjectMapper mapper;

    public ApplicationService(JobApplicationRepository applications, PortfolioProjectRepository projects,
                              ProjectDraftRepository drafts, GeminiService ai, ObjectMapper mapper) {
        this.applications = applications; this.projects = projects; this.drafts = drafts; this.ai = ai; this.mapper = mapper;
    }

    @Transactional
    public JobApplication create(ApplicationDtos.SaveRequest r) {
        return applications.save(new JobApplication(r.jobTitle(), r.companyName(), r.jobDescription(), r.candidateSummary()));
    }

    @Transactional
    public JobApplication createFromRawAndAnalyze(ApplicationDtos.AnalyzeRawRequest request) {
        GeminiService.ExtractedJob extracted = ai.extractAndAnalyzeJob(request.jobText(), request.candidateSummary());
        String title = normalize(extracted.jobTitle(), "未识别岗位", 250);
        String company = normalize(extracted.companyName(), "", 250);
        String description = normalize(extracted.jobDescription(), request.jobText(), 20000);
        JobApplication application = new JobApplication(title, company, description, request.candidateSummary());
        application.saveAnalysis(extracted.analysisJson());
        return applications.save(application);
    }

    @Transactional(readOnly = true)
    public JobApplication get(UUID id) { return applications.findById(id).orElseThrow(() -> new NotFoundException("求职申请不存在")); }

    @Transactional
    public JobApplication update(UUID id, ApplicationDtos.SaveRequest r) {
        JobApplication a = get(id); a.update(r.jobTitle(), r.companyName(), r.jobDescription(), r.candidateSummary()); return a;
    }

    @Transactional
    public JobApplication analyze(UUID id) {
        JobApplication a = get(id);
        String json = ai.analyzeJob(a.getJobTitle(), a.getCompanyName(), a.getJobDescription(), a.getCandidateSummary());
        a.saveAnalysis(json); return a;
    }

    @Transactional
    public JobApplication editAnalysis(UUID id, String json) {
        try { mapper.readTree(json); } catch (Exception e) { throw new IllegalArgumentException("岗位分析必须是有效 JSON"); }
        JobApplication a = get(id); a.editAnalysis(json); return a;
    }

    @Transactional
    public JobApplication select(UUID id, List<UUID> projectIds) {
        if (projectIds == null || projectIds.size() != 3 || new HashSet<>(projectIds).size() != 3) {
            throw new IllegalArgumentException("必须选择三个不同项目");
        }
        List<PortfolioProject> found = projects.findAllById(projectIds);
        if (found.size() != 3 || found.stream().anyMatch(p -> !p.isEnabled())) {
            throw new IllegalArgumentException("选择中包含不存在或已禁用的项目");
        }
        JobApplication a = get(id);
        a.selectProjects(projectIds.stream().map(UUID::toString).collect(Collectors.joining(",")));
        drafts.deleteAllByApplication_Id(id);
        return a;
    }

    private String normalize(String value, String fallback, int maxLength) {
        String result = value == null || value.isBlank() ? fallback : value.trim();
        return result.length() <= maxLength ? result : result.substring(0, maxLength);
    }
}
