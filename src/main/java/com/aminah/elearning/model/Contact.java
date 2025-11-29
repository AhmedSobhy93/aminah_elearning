package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Document(collection = "contacts")
//@Data
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable=false)
    private String name;
    private String email;
    private String subject;
    private String message;
}
