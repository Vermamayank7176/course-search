package com.verma.coursesearch.service;

import com.verma.coursesearch.document.CourseDocument;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchResult search(String q,
                               Integer minAge,
                               Integer maxAge,
                               String category,
                               String type,
                               Double minPrice,
                               Double maxPrice,
                               OffsetDateTime startDate,
                               String sort,
                               int page,
                               int size) {

        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        // Full-text: title + description
        if (q != null && !q.isBlank()) {
            MultiMatchQueryBuilder mmq = QueryBuilders.multiMatchQuery(q, "title", "description")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .fuzziness(Fuzziness.AUTO);
            bool.must(mmq);
        }

        // Exact filters
        if (category != null && !category.isBlank()) {
            bool.filter(QueryBuilders.termQuery("category", category));
        }
        if (type != null && !type.isBlank()) {
            bool.filter(QueryBuilders.termQuery("type", type));
        }

        // Range filters
        if (minAge != null) {
            bool.filter(QueryBuilders.rangeQuery("maxAge").gte(minAge));
        }
        if (maxAge != null) {
            bool.filter(QueryBuilders.rangeQuery("minAge").lte(maxAge));
        }
        if (minPrice != null || maxPrice != null) {
            RangeQueryBuilder priceRange = QueryBuilders.rangeQuery("price");
            if (minPrice != null) priceRange.gte(minPrice);
            if (maxPrice != null) priceRange.lte(maxPrice);
            bool.filter(priceRange);
        }

        if (startDate != null) {
            bool.filter(QueryBuilders.rangeQuery("nextSessionDate").gte(startDate.toString()));
        }

        NativeSearchQueryBuilder qb = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withPageable(PageRequest.of(page, size));

        // Sorting
        if ("priceAsc".equalsIgnoreCase(sort)) {
            qb.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            qb.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        } else {
            // default: upcoming
            qb.withSort(SortBuilders.fieldSort("nextSessionDate").order(SortOrder.ASC));
        }

        NativeSearchQuery query = qb.build();
        SearchHits<CourseDocument> hits = elasticsearchOperations.search(query, CourseDocument.class);

        long total = hits.getTotalHits();
        List<CourseDocument> courses = hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new SearchResult(total, courses);
    }

    // DTO
    public static record SearchResult(long total, List<CourseDocument> courses) {}
}
