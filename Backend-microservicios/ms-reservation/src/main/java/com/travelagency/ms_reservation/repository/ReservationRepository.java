package com.travelagency.ms_reservation.repository;

import com.travelagency.ms_reservation.dto.PackageRankingDTO;
import com.travelagency.ms_reservation.entity.ReservationEntity;
import com.travelagency.ms_reservation.entity.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // Count how many reservations currently are for a bundle
    long countByBundleId(Long bundleId);

    // Counts how many reservations a user has in a certain state (e.g. for FREQUENT_CLIENT)
    long countByUserEmailAndState(String userEmail, ReservationState state);

    List<ReservationEntity> findByUserEmailOrderByReservationDateDesc(String userEmail);

    boolean existsByUserEmail(String userEmail);

    List<ReservationEntity> findByStateAndReservationDateBefore(ReservationState state, LocalDate date);

    // E7: Package ranking
    @Query("SELECT r.bundleId AS bundleId, r.bundleName AS bundleName, " +
            "COUNT(r) AS totalReservations, " +
            "SUM(r.numberOfPassengers) AS totalPassengers, " +
            "SUM(r.totalAmount) AS totalRevenue " +
            "FROM ReservationEntity r " +
            "WHERE r.state <> :canceledState " +
            "AND r.reservationDate >= :startDate " +
            "AND r.reservationDate <= :endDate " +
            "GROUP BY r.bundleId, r.bundleName " +
            "ORDER BY totalReservations DESC")
    List<PackageRankingDTO> findPackageRanking(
            @Param("canceledState") ReservationState canceledState,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // E7: Sales by date range
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.reservationDate >= :startDate " +
            "AND r.reservationDate <= :endDate " +
            "AND r.state <> :canceledState " +
            "ORDER BY r.reservationDate ASC")
    List<ReservationEntity> findSalesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("canceledState") ReservationState canceledState);
}
