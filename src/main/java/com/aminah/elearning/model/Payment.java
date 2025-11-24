package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//@Data
//@Entity
//@Table(name = "payments")
@Document(collection = "payments")
@Data
public class Payment {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

//    /** Linked user */
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
    private String userId;

//    /** Linked enrollment */
//    @OneToOne
//    @JoinColumn(name = "enrollment_id", nullable = false)
    private String courseEnrollmentId;

    /** Amount paid */
    private Double amount;

    /** Payment status: PENDING, SUCCESS, FAILED */
    private String status;

    /** Payment gateway (PAYMOB, STRIPE, etc.) */
    private String gateway;

    private String courseId;
}
