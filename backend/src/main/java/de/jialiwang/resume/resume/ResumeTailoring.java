package de.jialiwang.resume.resume;

import java.util.List;

public record ResumeTailoring(
        String headline,
        List<ProjectPlan> projects,
        List<SkillPlan> skillGroups
) {
    public record ProjectPlan(int sourceIndex, List<Integer> itemNumbers) {}
    public record SkillPlan(String categoryId, List<String> skills) {}
}
