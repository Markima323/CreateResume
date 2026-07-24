package de.jialiwang.resume.resume;

import java.util.List;

public record ResumeTailoring(
        String headline,
        List<SkillPlan> skillGroups
) {
    public record SkillPlan(String categoryId, List<String> skills) {}
}
