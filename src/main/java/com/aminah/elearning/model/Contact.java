package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String subject;
    private String message;
}
