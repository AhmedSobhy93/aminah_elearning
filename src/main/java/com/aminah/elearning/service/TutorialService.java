package com.aminah.elearning.service;

import com.aminah.elearning.model.Section;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.repository.CourseRepository;
import com.aminah.elearning.repository.SectionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TutorialService {
    private final TutorialRepository tutorialRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public Page<Tutorial> getTutorialsForSection(Long sectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return tutorialRepository.findBySectionId(sectionId, pageable);
    }

//    public Page<Tutorial> getTutorialsForSection(Long sectionId) {
//        return tutorialRepository.findBySectionId(sectionId);
//    }

    public Tutorial getTutorial(Long id) {
        return tutorialRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Tutorial not found"));
    }

    public Tutorial save(Tutorial t) {
        return tutorialRepository.save(t);
    }

    public void delete(Long id) {
        tutorialRepository.deleteById(id);
    }

    public Page<Tutorial> getTutorialsForCourse(Long sectionId, int page, int size) {
        return tutorialRepository.findBySectionId(sectionId, PageRequest.of(page, size));
    }

    @Transactional
    public Tutorial addTutorialToSection(Long sectionId, Tutorial tutorial) {
        Section sec = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        tutorial.setSection(sec);
        tutorial.setOrderIndex(sec.getTutorials().size());

        sec.getTutorials().add(tutorial);

        return tutorialRepository.save(tutorial);
    }

}




