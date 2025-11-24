package com.aminah.elearning.repository;

import com.aminah.elearning.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
}
