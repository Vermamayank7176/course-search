package com.verma.coursesearch.controller;

import com.verma.coursesearch.document.CourseDocument;
import com.verma.coursesearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false, defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SearchService.SearchResult res = searchService.search(q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
        return Map.of(
                "total", res.total(),
                "courses", res.courses().stream().map(c -> Map.of(
                        "id", c.getId(),
                        "title", c.getTitle(),
                        "category", c.getCategory(),
                        "price", c.getPrice(),
                        "nextSessionDate", c.getNextSessionDate()
                )).toList()
        );
    }
}
