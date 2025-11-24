package com.aminah.elearning.repository;
import com.aminah.elearning.model.Video;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
    List<Video> findByCourseId(String courseId);
}