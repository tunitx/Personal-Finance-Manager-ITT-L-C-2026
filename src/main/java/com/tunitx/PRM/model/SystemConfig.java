package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "llm_provider", nullable = false, length = 50)
    private String llmProvider;

    @Column(name = "llm_api_key_enc", length = 500)
    private String llmApiKeyEnc;

    @Column(name = "scheduler_interval_hrs", nullable = false)
    private Integer schedulerIntervalHrs;

    @Column(name = "max_weekly_hours", nullable = false)
    private Integer maxWeeklyHours;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}