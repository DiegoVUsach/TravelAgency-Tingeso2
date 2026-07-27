package com.travelagency.ms_bundle.controller;

import com.travelagency.ms_bundle.entity.ExperienceTypeState;
import com.travelagency.ms_bundle.entity.SeasonTypeState;
import com.travelagency.ms_bundle.entity.CategoryTypeState;
import com.travelagency.ms_bundle.service.BundleService;
import com.travelagency.ms_bundle.entity.BundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bundle")
@CrossOrigin(originPatterns = "*")
public class BundleController {

    @Autowired
    BundleService bundleService;

    @GetMapping
    public ResponseEntity<List<BundleEntity>> getAllBundles() {
        List<BundleEntity> bundles = bundleService.findByPriceBundleGreaterThan(0);
        return ResponseEntity.ok(bundles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundleEntity> getBundleById(@PathVariable Long id) {
        BundleEntity bundle = bundleService.getBundleById(id);
        return ResponseEntity.ok(bundle);
    }

    @GetMapping("/sort/greaterThan/{price}")
    public List<BundleEntity> findByPriceBundleGreaterThan(@PathVariable("price") int price) {
        return bundleService.findByPriceBundleGreaterThan(price);
    }

    @PostMapping
    public ResponseEntity<BundleEntity> saveBundle(@RequestBody BundleEntity bundleEntity) {
        BundleEntity saved = bundleService.saveBundle(bundleEntity);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BundleEntity> updateBundle(@PathVariable Long id, @RequestBody BundleEntity bundleEntity) {
        BundleEntity updated = bundleService.updateBundle(id, bundleEntity);
        return ResponseEntity.ok(updated);
    }

    // E3 method, for bundle search
    // This endpoint is also exposed here so ms-search can call it internally
    @GetMapping("/search")
    public ResponseEntity<List<BundleEntity>> searchBundles(
            @RequestParam(required = false) String destiny,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ExperienceTypeState experience,
            @RequestParam(required = false) SeasonTypeState season,
            @RequestParam(required = false) CategoryTypeState category) {

        List<BundleEntity> results = bundleService.searchAvailableBundles(
                destiny, minPrice, maxPrice, duration, startDate, endDate, experience, season, category);

        return ResponseEntity.ok(results);
    }

    // ========== Internal Endpoints for ms-reservation ==========

    @PutMapping("/internal/{id}/decrease-slots")
    public ResponseEntity<Void> decreaseSlots(@PathVariable Long id, @RequestParam int amount) {
        bundleService.decreaseSlots(id, amount);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/internal/{id}/increase-slots")
    public ResponseEntity<Void> increaseSlots(@PathVariable Long id, @RequestParam int amount) {
        bundleService.increaseSlots(id, amount);
        return ResponseEntity.ok().build();
    }
}
