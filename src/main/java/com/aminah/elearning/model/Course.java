package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.*;
//import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Entity
//@Table(name="courses")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
@Document(collection = "courses")
@Data
public class Course {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private String id;

//    @Column(nullable=false)
    private String title;

//    @Column(nullable = false)
    private String courseName;

//    @Column(length=2000)
    private String description;

//    @Column(nullable=false)
    private Double price;

    private boolean published = false;

    private String videoUrl; // Cloudinary or external URL

    private LocalDateTime createdAt = LocalDateTime.now();

//    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseEnrollment> courseEnrollmentIds= new ArrayList<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id")
//    private User author; // DR
    private String authorUsername;

//    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<String> tutorialIds= new ArrayList<>();



//    public void addTutorial(Tutorial tutorial) {
//        tutorials.add(tutorial);
//        tutorial.setCourse(this);
//    }
}
