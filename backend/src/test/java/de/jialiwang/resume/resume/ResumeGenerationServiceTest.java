package de.jialiwang.resume.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jialiwang.resume.ai.GeminiService;
import de.jialiwang.resume.application.ApplicationService;
import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectdraft.ProjectDraftRepository;
import de.jialiwang.resume.projectdraft.ProjectLatexParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResumeGenerationServiceTest {
    @TempDir Path generationRoot;

    @Test
    void generatesDirectlyFromThreeValidProjectDescriptions() {
        ApplicationService applications = mock(ApplicationService.class);
        ProjectDraftRepository drafts = mock(ProjectDraftRepository.class);
        ResumeGenerationRepository generations = mock(ResumeGenerationRepository.class);
        LatexRenderer renderer = mock(LatexRenderer.class);
        GeminiService ai = mock(GeminiService.class);
        JobApplication application = new JobApplication("Manuell", "", "Direkte Eingabe", "");
        when(applications.get(application.getId())).thenReturn(application);
        when(generations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ResumeTailoring tailoring = tailoring();
        when(ai.tailorResume(eq(application), anyList())).thenReturn(tailoring);
        when(renderer.render(anyList(), eq(tailoring))).thenReturn("\\documentclass{article}\\begin{document}OK\\end{document}");
        ResumeGenerationService service = new ResumeGenerationService(generationRoot.toString(), applications, drafts,
                generations, renderer, new ProjectLatexParser(), new ObjectMapper(), ai);

        ResumeGeneration result = service.generateManual(application.getId(), List.of(project("A"), project("B"), project("C")));

        assertEquals(application.getId(), result.getApplicationId());
        assertEquals("Lebenslauf-Jiali Wang-Manuell.pdf", service.downloadName(application.getId(), true));
        verify(ai).tailorResume(eq(application), argThat(projects -> projects.size() == 3));
        verify(renderer).render(argThat(projects -> projects.size() == 3), eq(tailoring));
    }

    @Test
    void rejectsManualGenerationWhenAnyProjectIsInvalid() {
        ApplicationService applications = mock(ApplicationService.class);
        JobApplication application = new JobApplication("Manuell", "", "Direkte Eingabe", "");
        when(applications.get(application.getId())).thenReturn(application);
        ResumeGenerationService service = new ResumeGenerationService(generationRoot.toString(), applications,
                mock(ProjectDraftRepository.class), mock(ResumeGenerationRepository.class), mock(LatexRenderer.class),
                new ProjectLatexParser(), new ObjectMapper(), mock(GeminiService.class));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.generateManual(application.getId(), List.of(project("A"), "nur Text", project("C"))));

        assertEquals(true, error.getMessage().startsWith("项目 2："));
    }

    private String project(String title) {
        return "\\resumeProjectHeading {\\textbf{" + title + "} $|$ \\emph{Java}} {Einzelentwicklung} "
                + "\\resumeItemListStart "
                + "\\resumeItem{Eins} \\resumeItem{Zwei} \\resumeItem{Drei} \\resumeItem{Vier} "
                + "\\resumeItemListEnd";
    }

    private ResumeTailoring tailoring() {
        return new ResumeTailoring("Softwareentwicklerin mit Schwerpunkt Backend-Entwicklung",
                List.of(new ResumeTailoring.ProjectPlan(0, List.of(1, 2, 3, 4)),
                        new ResumeTailoring.ProjectPlan(1, List.of(1, 2, 3)),
                        new ResumeTailoring.ProjectPlan(2, List.of(1, 2))),
                ResumeProfile.SKILL_CATEGORIES.stream()
                        .map(category -> new ResumeTailoring.SkillPlan(category.id(), category.skills())).toList());
    }
}
