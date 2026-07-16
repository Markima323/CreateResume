package de.jialiwang.resume.projectdraft;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectDraftRepository extends JpaRepository<ProjectDraft, UUID> {
    List<ProjectDraft> findAllByApplication_IdOrderByPosition(UUID applicationId);
    Optional<ProjectDraft> findByApplication_IdAndPosition(UUID applicationId, short position);
    void deleteAllByApplication_Id(UUID applicationId);
}
