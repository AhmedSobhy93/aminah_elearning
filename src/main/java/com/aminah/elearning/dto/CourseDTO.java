package com.aminah.elearning.dto;

public record CourseDTO(
        Long id,
        String title,
        String courseName,
        Double price,
        Boolean published,
        String videoUrl,
        String description
) {}
