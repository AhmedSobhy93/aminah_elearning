package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//@Entity
//@Table(name = "contact")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
@Document(collection = "contacts")
@Data
public class Contact {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String email;
    private String subject;
    private String message;
}
