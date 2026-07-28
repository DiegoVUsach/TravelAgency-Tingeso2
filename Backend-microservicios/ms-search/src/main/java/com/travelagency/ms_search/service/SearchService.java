package com.travelagency.ms_search.service;

import com.travelagency.ms_search.model.CategoryTypeState;
import com.travelagency.ms_search.model.ExperienceTypeState;
import com.travelagency.ms_search.model.SeasonTypeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final RestTemplate restTemplate;

    @Autowired
    public SearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Performs a search by orchestrating a call to ms-bundle.
     * In a more complex scenario, this could query multiple services (e.g. flights + hotels)
     * and aggregate them. Here, it asks ms-bundle for the packages matching the criteria.
     */
    public List<Map<String, Object>> searchBundles(
            String destiny,
            Integer minPrice,
            Integer maxPrice,
            Integer duration,
            LocalDate startDate,
            LocalDate endDate,
            ExperienceTypeState experience,
            SeasonTypeState season,
            CategoryTypeState category) {

        // Build the URL to call ms-bundle
        // Note: the internal endpoint on ms-bundle is still /api/v1/bundle/search
        String baseUrl = "http://ms-bundle/api/v1/bundle/search";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);

        if (destiny != null) builder.queryParam("destiny", destiny);
        if (minPrice != null) builder.queryParam("minPrice", minPrice);
        if (maxPrice != null) builder.queryParam("maxPrice", maxPrice);
        if (duration != null) builder.queryParam("duration", duration);
        if (startDate != null) builder.queryParam("startDate", startDate.toString());
        if (endDate != null) builder.queryParam("endDate", endDate.toString());
        if (experience != null) builder.queryParam("experience", experience.name());
        if (season != null) builder.queryParam("season", season.name());
        if (category != null) builder.queryParam("category", category.name());

        // Call ms-bundle via Eureka.
        // We use ParameterizedTypeReference to parse the JSON array into a List of Maps
        // since we just want to pass the data through to the client without needing a full DTO class.
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }
}
