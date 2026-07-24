package de.jialiwang.resume.resume;

import de.jialiwang.resume.ai.GeminiService;
import de.jialiwang.resume.application.ApplicationService;
import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class MotivationLetterService {
    private final ApplicationService applications;
    private final ResumeGenerationService generations;
    private final MotivationLetterRepository letters;
    private final GeminiService ai;

    public MotivationLetterService(ApplicationService applications, ResumeGenerationService generations,
                                   MotivationLetterRepository letters, GeminiService ai) {
        this.applications = applications; this.generations = generations; this.letters = letters; this.ai = ai;
    }

    @Transactional
    public MotivationLetter generate(UUID applicationId, UUID generationId, MotivationLanguage language, String personalInfo) {
        if (personalInfo != null && personalInfo.length() > 6000) throw new IllegalArgumentException("补充个人信息不能超过 6000 个字符");
        JobApplication application = applications.get(applicationId);
        ResumeGeneration generation = generations.get(applicationId, generationId);
        try {
            String resume = Files.readString(Paths.get(generation.getTexPath()), StandardCharsets.UTF_8);
            GeminiService.GeneratedLetter result = ai.generateMotivationLetter(application, resume,
                    personalInfo == null ? "" : personalInfo.strip(), language);
            return letters.save(new MotivationLetter(application, generation, language,
                    personalInfo == null ? "" : personalInfo.strip(), result.subject(), result.content()));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("无法读取已生成的简历", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<MotivationLetter> latest(UUID applicationId, UUID generationId) {
        generations.get(applicationId, generationId);
        return letters.findFirstByApplication_IdAndGeneration_IdOrderByCreatedAtDesc(applicationId, generationId);
    }

    @Transactional(readOnly = true)
    public MotivationLetter get(UUID applicationId, UUID generationId, UUID letterId) {
        return letters.findByIdAndApplication_IdAndGeneration_Id(letterId, applicationId, generationId)
                .orElseThrow(() -> new NotFoundException("动机信不存在"));
    }
}
