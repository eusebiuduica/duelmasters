package org.example.duelmasters.Repositories;

import org.example.duelmasters.Models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
}
