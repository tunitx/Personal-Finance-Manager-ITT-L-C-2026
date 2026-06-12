package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_manager_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagerMapping {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;
}