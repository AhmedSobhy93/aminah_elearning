package com.aminah.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class CourseViewDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String level;
    private String authorName;
    private boolean enrolled;
    private int progress; // if enrolled
    private List<SectionDTO> sections;

    @Data
    @AllArgsConstructor
    public static class SectionDTO {
        private Long id;
        private String title;
        private List<TutorialDTO> tutorials;
        private String status; // NEW: "Not started" / "In Progress" / "Completed"
        private int progress;
    }


    @Data
    @AllArgsConstructor
    public static class TutorialDTO {
        private Long id;
        private String title;
        private String type;
        private boolean preview;
        private boolean read;
    }
}
