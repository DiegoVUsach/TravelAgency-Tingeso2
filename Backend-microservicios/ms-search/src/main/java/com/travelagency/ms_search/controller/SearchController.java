package com.travelagency.ms_search.controller;

import com.travelagency.ms_search.model.CategoryTypeState;
import com.travelagency.ms_search.model.ExperienceTypeState;
import com.travelagency.ms_search.model.SeasonTypeState;
import com.travelagency.ms_search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bundle") // We map this here so the gateway routes /api/v1/bundle/search to us
@CrossOrigin(originPatterns = "*")
public class SearchController {

    @Autowired
    SearchService searchService;

    // E3 method, for bundle search
    // This is the primary entry point for the frontend when searching for packages
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchBundles(
            @RequestParam(required = false) String destiny,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ExperienceTypeState experience,
            @RequestParam(required = false) SeasonTypeState season,
            @RequestParam(required = false) CategoryTypeState category) {

        List<Map<String, Object>> results = searchService.searchBundles(
                destiny, minPrice, maxPrice, duration, startDate, endDate, experience, season, category);

        return ResponseEntity.ok(results);
    }
}
