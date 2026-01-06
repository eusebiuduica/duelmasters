package org.example.duelmasters.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.duelmasters.Utils.AuditAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User actorUser;

    @Column(name = "actor_username", nullable = false)
    private String actorUsername; // snapshot

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(name = "target_username")
    private String targetUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "action_time", nullable = false, updatable = false)
    private LocalDateTime actionTime;

    @PrePersist
    protected void onCreate() {
        this.actionTime = LocalDateTime.now();
    }
}
