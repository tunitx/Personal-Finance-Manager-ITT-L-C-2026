package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}