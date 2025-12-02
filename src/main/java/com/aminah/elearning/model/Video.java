package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "videos")
@Data
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String title;
    private String description;
    private String videoUrl; // stored link (e.g., local or S3 path)
}
