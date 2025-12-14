package com.aminah.elearning.service;

import com.aminah.elearning.dto.QuizQuestionDto;
import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TutorialService {
    private final TutorialRepository tutorialRepository;
    private final SectionRepository sectionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final TutorialProgressRepository tutorialProgressRepository;


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

    public void updateQuizQuestions(Tutorial tutorial, String quizJson) {
        // Parse JSON into DTOs
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<QuizQuestionDto> dtos = Arrays.asList(mapper.readValue(quizJson, QuizQuestionDto[].class));

            // Remove old questions
            quizQuestionRepository.deleteByTutorialId(tutorial.getId());
            System.out.println("dtos.getFirst()" + dtos.getFirst());
            // Add new questions
            for (QuizQuestionDto dto : dtos) {
                QuizQuestion q = new QuizQuestion();
                q.setQuestion(dto.getQuestion());
                q.setOptions(dto.getOptions());
                q.setCorrectOptionIndex(dto.getCorrectOptionIndex());
                q.setTutorial(tutorial); // ✅ tutorial now has ID
                quizQuestionRepository.save(q);
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid quiz JSON", e);
        }


}
        public TutorialProgress getProgress (User user, Tutorial tutorial){
            return tutorialProgressRepository.findByUserIdAndTutorialId(user.getId(), tutorial.getId())
                    .orElseGet(() -> tutorialProgressRepository.save(
                            new TutorialProgress(user, tutorial, false)
                    ));
        }

        public void markComplete (User user, Tutorial tutorial){
            TutorialProgress progress = getProgress(user, tutorial);
            progress.setCompleted(true);
            tutorialProgressRepository.save(progress);
        }

        public int submitQuiz (User user, Tutorial tutorial, Map < Long, Integer > answers){
//            Tutorial tutorial = getTutorial(tutorial);

            int score = 0;

            for (QuizQuestion q : tutorial.getQuizQuestions()) {
                int selected = answers.get(q.getId());
                if (q.getCorrectOptionIndex() == selected)
                    score++;
            }

            markComplete(user, tutorial);
            return score;
        }
    }



//    @Transactional
//    public void updateQuizQuestions(Tutorial tutorial, String quizQuestionsJson) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//
//            // Your JSON will be a list of:
//            // {
//            //   "question": "...",
//            //   "options": ["A", "B", "C"],
//            //   "correctOptionIndex": 1
//            // }
//            List<Map<String, Object>> questionsList = mapper.readValue(
//                    quizQuestionsJson,
//                    new TypeReference<List<Map<String, Object>>>() {}
//            );
//
//            // Remove existing questions
//            tutorial.getQuizQuestions().clear();
//
//            // Add new questions
//            for (Map<String, Object> q : questionsList) {
//
//                QuizQuestion question = new QuizQuestion();
//                question.setTutorial(tutorial);
//                question.setQuestion((String) q.get("question"));
//
//                // Extract options list
//                List<String> options = (List<String>) q.get("options");
//                question.setOptions(options);
//
//                // Extract correct index
//                Integer correctIndex = (Integer) q.get("correctOptionIndex");
//                question.setCorrectOptionIndex(correctIndex);
//
//                tutorial.getQuizQuestions().add(question);
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid quiz questions JSON format: " + e.getMessage());
//        }
//    }





