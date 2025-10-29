package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "videos")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id")
    private Integer courseId;

    private String title;
    private String description;
    private String videoUrl; // stored link (e.g., local or S3 path)
}
