package de.jialiwang.resume.projectdraft;

import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectcatalog.PortfolioProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PromptFactoryTest {
    @Test
    void mapsAnalysisAndOriginalJobDescriptionToTheirPromptSections() {
        JobApplication application = new JobApplication("Backend Engineer", "Beispiel GmbH",
                "ORIGINAL_JOB_DESCRIPTION", "");
        application.saveAnalysis("ANALYSIS_JSON");

        String prompt = new PromptFactory().create(application, mock(PortfolioProject.class), 2);

        assertThat(prompt).contains("目标岗位分析：\nANALYSIS_JSON");
        assertThat(prompt).contains("<job_description>\nORIGINAL_JOB_DESCRIPTION\n</job_description>");
        assertThat(prompt).contains("3 个 \\resumeItem").doesNotContain("\\resumeItem{[内容 4]}");
    }
}
