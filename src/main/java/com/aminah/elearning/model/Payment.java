package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Linked user */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Linked enrollment */
    @OneToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CourseEnrollment courseEnrollment;

    /** Amount paid */
    private Double amount;

    /** Payment status: PENDING, SUCCESS, FAILED */
    private String status;

    /** Payment gateway (PAYMOB, STRIPE, etc.) */
    private String gateway;
}
