package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable=false)
    private String title;

    @Column(nullable = false)
    private String courseName;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable=false)
    private Double price;

    private boolean published = false;

    private String videoUrl; // Cloudinary or external URL

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseEnrollment> courseEnrollmentIds= new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author; // DR
    private String authorUsername;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    private List<Tutorial> tutorials = new ArrayList<>();




//    public void addTutorial(Tutorial tutorial) {
//        tutorials.add(tutorial);
//        tutorial.setCourse(this);
//    }
}
