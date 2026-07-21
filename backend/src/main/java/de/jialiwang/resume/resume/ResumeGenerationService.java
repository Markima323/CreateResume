package de.jialiwang.resume.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jialiwang.resume.ai.GeminiService;
import de.jialiwang.resume.application.ApplicationService;
import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.common.NotFoundException;
import de.jialiwang.resume.projectdraft.ProjectDraft;
import de.jialiwang.resume.projectdraft.ProjectDraftRepository;
import de.jialiwang.resume.projectdraft.ProjectLatexParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ResumeGenerationService {
    private final Path generationRoot;
    private final ApplicationService applications;
    private final ProjectDraftRepository drafts;
    private final ResumeGenerationRepository generations;
    private final LatexRenderer renderer;
    private final ProjectLatexParser parser;
    private final ObjectMapper mapper;
    private final GeminiService ai;

    public ResumeGenerationService(@Value("${app.generation-dir}") String generationDir, ApplicationService applications,
                                   ProjectDraftRepository drafts, ResumeGenerationRepository generations,
                                   LatexRenderer renderer, ProjectLatexParser parser, ObjectMapper mapper, GeminiService ai) {
        this.generationRoot = Paths.get(generationDir).toAbsolutePath().normalize();
        this.applications = applications; this.drafts = drafts; this.generations = generations;
        this.renderer = renderer; this.parser = parser; this.mapper = mapper; this.ai = ai;
    }

    @Transactional
    public ResumeGeneration generate(UUID applicationId) {
        JobApplication app = applications.get(applicationId);
        List<ProjectDraft> projectDrafts = drafts.findAllByApplication_IdOrderByPosition(applicationId);
        if (projectDrafts.size() != 3 || projectDrafts.stream().anyMatch(d -> !d.isApproved() || d.getParsedJson() == null)) {
            throw new IllegalArgumentException("请先校验并确认三个项目内容");
        }
        List<ProjectLatexParser.ParsedProject> parsed = projectDrafts.stream().map(d -> {
            try { return mapper.readValue(d.getParsedJson(), ProjectLatexParser.ParsedProject.class); }
            catch (Exception e) { throw new IllegalStateException(e); }
        }).toList();
        return generate(app, parsed);
    }

    @Transactional
    public ResumeGeneration generateManual(UUID applicationId, List<String> projects) {
        JobApplication app = applications.get(applicationId);
        if (projects == null || projects.size() != 3) throw new IllegalArgumentException("必须填写三个项目描述");
        List<ProjectLatexParser.ParsedProject> parsed = projects.stream().map(parser::parse).toList();
        String errors = java.util.stream.IntStream.range(0, parsed.size())
                .filter(index -> !parsed.get(index).valid())
                .mapToObj(index -> "项目 " + (index + 1) + "：" + String.join("；", parsed.get(index).errors()))
                .collect(java.util.stream.Collectors.joining(" | "));
        if (!errors.isBlank()) throw new IllegalArgumentException(errors);
        return generate(app, parsed);
    }

    private ResumeGeneration generate(JobApplication app, List<ProjectLatexParser.ParsedProject> parsed) {
        try {
            UUID generationId = UUID.randomUUID();
            Path dir = generationRoot.resolve(app.getId().toString()).resolve(generationId.toString()).normalize();
            ensureInsideRoot(dir); Files.createDirectories(dir);
            Path tex = dir.resolve("resume.tex");
            ResumeTailoring tailoring = ai.tailorResume(app, parsed);
            Files.writeString(tex, renderer.render(parsed, tailoring), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            ResumeGeneration generation = generations.save(new ResumeGeneration(generationId, app, tex.toString()));
            compile(dir, generation);
            app.markGenerated();
            return generation;
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("生成简历失败：" + e.getMessage(), e); }
    }

    private void compile(Path dir, ResumeGeneration generation) {
        try {
            Process process = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", "-halt-on-error", "resume.tex")
                    .directory(dir.toFile()).redirectErrorStream(true).redirectOutput(dir.resolve("compile.log").toFile()).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) { process.destroyForcibly(); generation.failed("LaTeX 编译超时；TeX 文件已生成"); return; }
            Path pdf = dir.resolve("resume.pdf");
            if (process.exitValue() == 0 && Files.exists(pdf)) generation.pdfReady(pdf.toString());
            else generation.failed("LaTeX 编译失败；请下载 TeX 检查内容");
        } catch (Exception e) {
            generation.failed("当前环境没有 pdflatex；TeX 文件已生成，可通过 Docker 构建 PDF");
        }
    }

    @Transactional(readOnly = true)
    public ResumeGeneration get(UUID applicationId, UUID generationId) {
        return generations.findByIdAndApplication_Id(generationId, applicationId)
                .orElseThrow(() -> new NotFoundException("生成记录不存在"));
    }
    public FileSystemResource file(UUID applicationId, UUID generationId, boolean pdf) {
        ResumeGeneration g = get(applicationId, generationId);
        String path = pdf ? g.getPdfPath() : g.getTexPath();
        if (path == null) throw new NotFoundException(pdf ? "PDF 尚未生成" : "TeX 不存在");
        Path resolved = Paths.get(path).toAbsolutePath().normalize(); ensureInsideRoot(resolved);
        if (!Files.isRegularFile(resolved)) throw new NotFoundException("生成文件不存在");
        return new FileSystemResource(resolved);
    }
    public String downloadName(UUID applicationId, boolean pdf) {
        String title = applications.get(applicationId).getJobTitle();
        String safeTitle = title == null ? "Ohne-Stellenbezeichnung" : title
                .replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "-").trim();
        if (safeTitle.isBlank()) safeTitle = "Ohne-Stellenbezeichnung";
        if (safeTitle.length() > 100) safeTitle = safeTitle.substring(0, 100).trim();
        return "Lebenslauf-Jiali Wang-" + safeTitle + (pdf ? ".pdf" : ".tex");
    }
    private void ensureInsideRoot(Path path) { if (!path.startsWith(generationRoot)) throw new IllegalArgumentException("无效生成路径"); }
}
