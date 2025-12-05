package com.aminah.elearning.service;

import com.aminah.elearning.model.Section;
import com.aminah.elearning.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository sectionRepository;

    public Section save(Section section){ return sectionRepository.save(section); }
    public void delete(Long id){ sectionRepository.deleteById(id); }
    public List<Section> getSectionsByCourse(Long courseId){
        return sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }
}
