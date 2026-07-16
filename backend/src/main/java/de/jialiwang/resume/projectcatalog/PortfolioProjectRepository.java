package de.jialiwang.resume.projectcatalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, UUID> {
    List<PortfolioProject> findAllByEnabledTrueOrderByNameZh();
}
