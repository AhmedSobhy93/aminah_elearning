package com.aminah.elearning.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

//@Data
//@NoArgsConstructor
//@Entity
//@Lazy(value = false)
//@Table(name = "course_enrollment")
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = CourseEnrollment.class)
@Document(collection = "course_enrollments")
@Data
public class CourseEnrollment {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /** The user who enrolled in the course */
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
    private String courseEnrollmentUserId;

    /** The course being enrolled in */
//    @ManyToOne
//    @JoinColumn(name = "course_id", nullable = false)
    private String courseId;


//    @Column(name = "enrollment_date", updatable = false)
    private LocalDateTime enrollmentDate = LocalDateTime.now();

//    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, SUCCESS, FAILED

//    @Column(name = "progress_percentage")
    private Double progressPercentage = 0.0;

//    @Column(name = "completion_status")
    private Boolean completed = false;
    private Boolean certificateIssued = false;


}