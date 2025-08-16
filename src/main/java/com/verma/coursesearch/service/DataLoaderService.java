package com.verma.coursesearch.service;

import com.verma.coursesearch.document.CourseDocument;
import com.verma.coursesearch.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoaderService {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    public void loadSampleData() {
        try {
            // do not re-index if already present (optional)
            if (courseRepository.count() > 0) {
                System.out.println("Courses index already populated. Skipping sample data load.");
                return;
            }

            InputStream is = new ClassPathResource("sample-courses.json").getInputStream();
            List<CourseDocument> courses = objectMapper.readValue(is, new TypeReference<List<CourseDocument>>() {});
            courseRepository.saveAll(courses);
            System.out.println("Indexed " + courses.size() + " courses into Elasticsearch.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
