package com.travelagency.ms_reports.service;

import com.travelagency.ms_reports.dto.PackageRankingDTO;
import com.travelagency.ms_reports.dto.ReservationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RestTemplate restTemplate;

    public List<ReservationDTO> getSalesReport(LocalDate startDate, LocalDate endDate) {
        String url = "http://ms-reservation/api/v1/reservations/reports/sales";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startDate", startDate.toString())
                .queryParam("endDate", endDate.toString());

        ResponseEntity<List<ReservationDTO>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReservationDTO>>() {}
        );

        return response.getBody();
    }

    public List<PackageRankingDTO> getPackageRanking(LocalDate startDate, LocalDate endDate) {
        String url = "http://ms-reservation/api/v1/reservations/reports/ranking";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startDate", startDate.toString())
                .queryParam("endDate", endDate.toString());

        ResponseEntity<List<PackageRankingDTO>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PackageRankingDTO>>() {}
        );

        return response.getBody();
    }
}
