package com.aminah.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private boolean enrolled;
    private int durationHours;
    private String level;
    private int progress;

    // constructor
    public CourseDTO(Long id, String title, String description, Double price,
                     boolean enrolled, int durationHours, String level, int progress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.enrolled = enrolled;
        this.durationHours = durationHours;
        this.level = level;
        this.progress = progress;
    }


}
