package de.jialiwang.resume.resume;

import java.util.List;

public final class ResumeProfile {
    private ResumeProfile() {}

    public record SkillCategory(String id, String label, List<String> skills) {}

    public static final List<SkillCategory> SKILL_CATEGORIES = List.of(
            new SkillCategory("languages", "Programmiersprachen",
                    List.of("Java", "Python", "C/C++", "SQL", "JavaScript", "HTML/CSS")),
            new SkillCategory("backend", "Backend & Frameworks",
                    List.of("Spring Boot", "Spring Security", "Spring Data JPA", "WebClient", "Flask", "FastAPI", "Node.js", "REST APIs", "SSE")),
            new SkillCategory("frontend", "Frontend",
                    List.of("React", "Vue.js", "Vite", "Element Plus", "Axios")),
            new SkillCategory("ai", "KI & Machine Learning",
                    List.of("PyTorch", "TensorFlow", "CUDA", "OpenCV", "ONNX Runtime", "InsightFace", "ComfyUI", "DeepLabCut", "Hunyuan3D", "Ollama", "Gemini API", "lokale LLM-Inferenz")),
            new SkillCategory("data", "Datenverarbeitung",
                    List.of("JSON Schema", "pandas", "NumPy", "openpyxl", "PyMuPDF", "python-docx", "CSV/H5/GLB")),
            new SkillCategory("databases", "Datenbanken",
                    List.of("PostgreSQL", "MySQL", "Flyway", "JPA/Hibernate", "Datenmodellierung")),
            new SkillCategory("devops", "DevOps & Tools",
                    List.of("Git", "Maven", "Docker", "Docker Compose", "Nginx", "Conda", "PyInstaller", "Linux", "Windows"))
    );
}
