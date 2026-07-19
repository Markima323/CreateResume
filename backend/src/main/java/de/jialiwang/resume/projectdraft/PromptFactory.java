package de.jialiwang.resume.projectdraft;

import de.jialiwang.resume.application.JobApplication;
import de.jialiwang.resume.projectcatalog.PortfolioProject;
import org.springframework.stereotype.Component;

@Component
public class PromptFactory {
    public String create(JobApplication app, PortfolioProject project) {
        return """
                你是一名熟悉德国 IT 招聘和 ATS 简历的编辑。

                任务：
                请分析当前项目的代码，这个项目是我实际开发的。并且根据以下规则写贴合目标岗位的德文简历项目介绍。


                严格规则：
                1. 只能使用“项目事实”中明确存在的信息，不得新增技术、职责、用户数量、性能数据或商业成果。
                2. 输出恰好 1 个 \\resumeProjectHeading 和 4 个 \\resumeItem，不多不少。
                3. 每条使用专业、简洁、以行动为导向的德文；避免空泛形容词和重复内容。
                4. 优先强调与岗位职责直接相关的架构、实现、集成、部署和可验证结果。
                5. 技术栈只列实际使用且对目标岗位有意义的技术。
                6. 正确转义 LaTeX 特殊字符；不要输出 Markdown 代码块和额外解释。
                7. 若目标岗位要求在项目事实中没有依据，不得声称掌握，只能省略。

                输出模板：
                \\resumeProjectHeading
                  {\\textbf{[德文项目名]} $|$ \\emph{[技术栈]}}
                  {[项目类型/角色]}
                \\resumeItemListStart
                  \\resumeItem{[内容 1]}
                  \\resumeItem{[内容 2]}
                  \\resumeItem{[内容 3]}
                  \\resumeItem{[内容 4]}
                \\resumeItemListEnd

                目标岗位分析：
                %s

                目标岗位原文：
                <job_description>
                %s
                </job_description>
                """.formatted(project.getNameZh(), safe(project.getNameDe()), project.getRoleText(), project.getSummary(),
                project.getTechnologies(), project.getResponsibilities(), project.getOutcomes(), project.getFacts(),
                safe(app.getAnalysisEditedJson()), app.getJobDescription());
    }
    private String safe(String value) { return value == null ? "" : value; }
}
