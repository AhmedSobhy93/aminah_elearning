package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//@Data
//@Entity
//@Table(name = "videos")
@Document(collection = "videos")
@Data
public class Video {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

//    @Column(name = "course_id")
    private String courseId;

    private String title;
    private String description;
    private String videoUrl; // stored link (e.g., local or S3 path)
}
