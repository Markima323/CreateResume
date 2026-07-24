package de.jialiwang.resume.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MotivationLetterRepository extends JpaRepository<MotivationLetter, UUID> {
    Optional<MotivationLetter> findFirstByApplication_IdAndGeneration_IdOrderByCreatedAtDesc(UUID applicationId, UUID generationId);
    Optional<MotivationLetter> findByIdAndApplication_IdAndGeneration_Id(UUID id, UUID applicationId, UUID generationId);
}
