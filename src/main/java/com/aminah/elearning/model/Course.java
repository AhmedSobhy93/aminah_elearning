package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Column(nullable = false)
    private String courseName;

    @Column(length=2000)
    private String description;

    @Column(nullable=false)
    private Double price;

    private boolean published = false;

    private String videoUrl; // Cloudinary or external URL

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "course")
    private List<CourseEnrollment> enrollments;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author; // DR

    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Tutorial> tutorials = new ArrayList<>();
}
