package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "timesheet_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "hours_logged", nullable = false)
    private Integer hoursLogged;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "entry_tags",
            joinColumns = @JoinColumn(name = "entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<ActivityTag> tags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}