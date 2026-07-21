package de.jialiwang.resume.resume;

import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.application.JobApplicationRepository;
import de.jialiwang.resume.common.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryService {
    public record Entry(UUID applicationId, String jobTitle, String companyName,
                        UUID generationId, OffsetDateTime generatedAt) {}

    private final Path generationRoot;
    private final JobApplicationRepository applications;
    private final ResumeGenerationRepository generations;

    public HistoryService(@Value("${app.generation-dir}") String generationDir,
                          JobApplicationRepository applications,
                          ResumeGenerationRepository generations) {
        this.generationRoot = Paths.get(generationDir).toAbsolutePath().normalize();
        this.applications = applications;
        this.generations = generations;
    }

    @Transactional(readOnly = true)
    public List<Entry> list() {
        LinkedHashMap<UUID, Entry> latest = new LinkedHashMap<>();
        for (ResumeGeneration generation : generations.findAllByStatusOrderByCreatedAtDesc("PDF_READY")) {
            JobApplication application = generation.getApplication();
            latest.putIfAbsent(application.getId(), new Entry(application.getId(), application.getJobTitle(),
                    application.getCompanyName(), generation.getId(), generation.getCreatedAt()));
        }
        return List.copyOf(latest.values());
    }

    @Transactional
    public void delete(UUID applicationId) {
        JobApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("历史记录不存在"));
        deleteGenerationDirectory(applicationId);
        applications.delete(application);
        applications.flush();
    }

    private void deleteGenerationDirectory(UUID applicationId) {
        Path directory = generationRoot.resolve(applicationId.toString()).normalize();
        if (!directory.startsWith(generationRoot) || directory.getParent() == null
                || !directory.getParent().equals(generationRoot)) {
            throw new IllegalArgumentException("无效生成目录");
        }
        if (!Files.exists(directory)) return;
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException("删除简历文件失败：" + e.getMessage(), e);
        }
    }
}
