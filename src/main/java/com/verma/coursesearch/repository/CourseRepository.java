package com.verma.coursesearch.repository;

import com.verma.coursesearch.document.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
    // Spring Data repo for simple CRUD; we use ElasticsearchOperations for custom queries.
}
