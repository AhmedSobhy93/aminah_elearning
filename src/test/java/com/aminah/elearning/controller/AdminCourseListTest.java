package com.aminah.elearning.controller;

import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminCourseListTest {
    @Test
    void firstPageIsNotSkippedAndNegativePagesAreBounded() {
        CourseService courses = mock(CourseService.class);
        when(courses.getCourses("", 0, 5)).thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 5)));
        var controller = new AdminController(mock(UserService.class), courses,
                mock(UserRepository.class), mock(CourseEnrollmentService.class));
        var model = new ExtendedModelMap();
        assertThat(controller.list(model, -1)).isEqualTo("admin/course-list");
        verify(courses).getCourses("", 0, 5);
        assertThat(model.get("totalPages")).isEqualTo(0);
    }
}
