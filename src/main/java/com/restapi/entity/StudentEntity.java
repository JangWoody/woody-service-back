package com.restapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_info")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}