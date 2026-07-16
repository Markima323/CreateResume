package de.jialiwang.resume.projectdraft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jialiwang.resume.application.ApplicationService;
import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectcatalog.PortfolioProject;
import de.jialiwang.resume.projectcatalog.PortfolioProjectRepository;
import de.jialiwang.resume.projectcatalog.ProjectDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DraftService {
    private final ProjectDraftRepository drafts;
    private final PortfolioProjectRepository projects;
    private final ApplicationService applications;
    private final PromptFactory prompts;
    private final ProjectLatexParser parser;
    private final ObjectMapper mapper;

    public DraftService(ProjectDraftRepository drafts, PortfolioProjectRepository projects, ApplicationService applications,
                        PromptFactory prompts, ProjectLatexParser parser, ObjectMapper mapper) {
        this.drafts = drafts; this.projects = projects; this.applications = applications;
        this.prompts = prompts; this.parser = parser; this.mapper = mapper;
    }

    @Transactional
    public List<DraftDtos.View> initialize(UUID applicationId) {
        JobApplication app = applications.get(applicationId);
        List<UUID> ids = selectedIds(app);
        if (ids.size() != 3) throw new IllegalArgumentException("请先选择三个项目");
        List<ProjectDraft> existing = drafts.findAllByApplication_IdOrderByPosition(applicationId);
        for (short i = 0; i < 3; i++) {
            PortfolioProject project = projects.findById(ids.get(i)).orElseThrow();
            final short position = (short) (i + 1);
            ProjectDraft draft = existing.stream().filter(d -> d.getPosition() == position).findFirst().orElse(null);
            if (draft == null) drafts.save(new ProjectDraft(app, project, position, prompts.create(app, project)));
            else if (!draft.getProject().getId().equals(project.getId())) draft.replaceProject(project, prompts.create(app, project));
        }
        return views(applicationId);
    }

    @Transactional(readOnly = true)
    public List<DraftDtos.View> views(UUID applicationId) {
        applications.get(applicationId);
        return drafts.findAllByApplication_IdOrderByPosition(applicationId).stream().map(this::view).toList();
    }

    @Transactional
    public DraftDtos.View save(UUID applicationId, short position, DraftDtos.ContentRequest request) {
        ProjectDraft draft = drafts.findByApplication_IdAndPosition(applicationId, position)
                .orElseThrow(() -> new IllegalArgumentException("请先生成三个项目 Prompt"));
        ProjectLatexParser.ParsedProject parsed = parser.parse(request.latex());
        boolean approved = request.approve() && parsed.valid();
        try {
            draft.saveContent(request.latex(), mapper.writeValueAsString(parsed), mapper.writeValueAsString(parsed.errors()), approved);
        } catch (Exception e) { throw new IllegalStateException("无法保存项目内容", e); }
        if (drafts.findAllByApplication_IdOrderByPosition(applicationId).stream().allMatch(ProjectDraft::isApproved)) {
            applications.get(applicationId).markContentReady();
        }
        return view(draft);
    }

    private DraftDtos.View view(ProjectDraft d) {
        try {
            ProjectLatexParser.ParsedProject parsed = d.getParsedJson() == null ? null : mapper.readValue(d.getParsedJson(), ProjectLatexParser.ParsedProject.class);
            List<String> errors = d.getValidationErrors() == null ? List.of() : mapper.readValue(d.getValidationErrors(), new TypeReference<>() {});
            return new DraftDtos.View(d.getId(), d.getPosition(), ProjectDto.from(d.getProject()), d.getGeneratedPrompt(),
                    d.getPastedLatex(), parsed, errors, d.isApproved(), d.getUpdatedAt());
        } catch (Exception e) { throw new IllegalStateException("无法读取项目草稿", e); }
    }

    private List<UUID> selectedIds(JobApplication app) {
        if (app.getSelectedProjectIds() == null || app.getSelectedProjectIds().isBlank()) return List.of();
        return Arrays.stream(app.getSelectedProjectIds().split(",")).map(UUID::fromString).toList();
    }
}
