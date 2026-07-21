package de.jialiwang.resume.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ResumeGenerationRepository extends JpaRepository<ResumeGeneration, UUID> {
    Optional<ResumeGeneration> findByIdAndApplication_Id(UUID id, UUID applicationId);
    List<ResumeGeneration> findAllByStatusOrderByCreatedAtDesc(String status);
}
