package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Section;
import com.aminah.elearning.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository sectionRepository;

    public Section save(Section section){ return sectionRepository.save(section); }
    public void delete(Long id){ sectionRepository.deleteById(id); }
    public List<Section> getSectionsByCourse(Long courseId){
        return sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    public Section getSection(Long id) {
        return sectionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Section not found"));
    }

    public Section createSection(Course course, String title) {
        Section s = new Section();
        s.setTitle(title);
        s.setCourse(course);
        s.setOrderIndex(course.getSections().size());
        return sectionRepository.save(s);
    }
    public List<Section> getSectionsForCourse(Long courseId) {
        return sectionRepository.findByCourseIdOrderByIdAsc(courseId);
    }
}
