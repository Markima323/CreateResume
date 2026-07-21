package de.jialiwang.resume.resume;

import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.application.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HistoryServiceTest {
    @TempDir Path generationRoot;

    @Test
    void listsOnlyLatestPdfForEachApplication() {
        JobApplicationRepository applications = mock(JobApplicationRepository.class);
        ResumeGenerationRepository generations = mock(ResumeGenerationRepository.class);
        JobApplication first = new JobApplication("Backend Developer", "A GmbH", "Beschreibung", "");
        JobApplication second = new JobApplication("Network Engineer", "B GmbH", "Beschreibung", "");
        ResumeGeneration latest = pdf(first);
        ResumeGeneration older = pdf(first);
        ResumeGeneration another = pdf(second);
        when(generations.findAllByStatusOrderByCreatedAtDesc("PDF_READY"))
                .thenReturn(List.of(latest, older, another));

        List<HistoryService.Entry> result = new HistoryService(generationRoot.toString(), applications, generations).list();

        assertThat(result).extracting(HistoryService.Entry::jobTitle)
                .containsExactly("Backend Developer", "Network Engineer");
        assertThat(result.getFirst().generationId()).isEqualTo(latest.getId());
    }

    @Test
    void deletesApplicationAndItsCompleteGenerationDirectory() throws Exception {
        JobApplicationRepository applications = mock(JobApplicationRepository.class);
        JobApplication application = new JobApplication("Backend Developer", "", "Beschreibung", "");
        when(applications.findById(application.getId())).thenReturn(Optional.of(application));
        Path directory = Files.createDirectories(generationRoot.resolve(application.getId().toString()).resolve("generation"));
        Files.writeString(directory.resolve("resume.pdf"), "pdf");

        new HistoryService(generationRoot.toString(), applications, mock(ResumeGenerationRepository.class))
                .delete(application.getId());

        assertThat(generationRoot.resolve(application.getId().toString())).doesNotExist();
        verify(applications).delete(application);
        verify(applications).flush();
    }

    private ResumeGeneration pdf(JobApplication application) {
        UUID id = UUID.randomUUID();
        ResumeGeneration generation = new ResumeGeneration(id, application, generationRoot.resolve(id + ".tex").toString());
        generation.pdfReady(generationRoot.resolve(id + ".pdf").toString());
        return generation;
    }
}
