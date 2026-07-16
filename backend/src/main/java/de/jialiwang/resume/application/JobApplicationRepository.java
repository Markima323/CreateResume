package de.jialiwang.resume.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {}
