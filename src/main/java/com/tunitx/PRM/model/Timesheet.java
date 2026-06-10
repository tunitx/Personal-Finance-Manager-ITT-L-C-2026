package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL)
    private List<TimesheetEntry> entries;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}