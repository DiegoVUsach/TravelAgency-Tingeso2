package com.travelagency.ms_reservation.service;

import com.travelagency.ms_reservation.dto.*;
import com.travelagency.ms_reservation.entity.DiscountConfigEntity;
import com.travelagency.ms_reservation.entity.ReservationEntity;
import com.travelagency.ms_reservation.entity.ReservationState;
import com.travelagency.ms_reservation.repository.DiscountConfigRepository;
import com.travelagency.ms_reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DiscountConfigRepository discountConfigRepository;
    private final RestTemplate restTemplate;

    // HTTP calls to ms-bundle
    private BundleDTO getBundleFromMsBundle(Long bundleId) {
        String url = "http://ms-bundle/api/v1/bundle/" + bundleId;
        try {
            return restTemplate.getForObject(url, BundleDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Bundle not found or ms-bundle is down. ID: " + bundleId);
        }
    }

    private void decreaseBundleSlots(Long bundleId, int amount) {
        String url = "http://ms-bundle/api/v1/bundle/internal/" + bundleId + "/decrease-slots?amount=" + amount;
        restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
    }

    private void increaseBundleSlots(Long bundleId, int amount) {
        String url = "http://ms-bundle/api/v1/bundle/internal/" + bundleId + "/increase-slots?amount=" + amount;
        restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
    }

    // ---------- Quoting Logic ----------

    public ReservationResponseDTO calculateQuote(ReservationRequestDTO request, String email) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("The cart cannot be empty.");
        }

        double globalDiscount = 0.0;
        List<DiscountDetailDTO> appliedDiscounts = new ArrayList<>();
        int multiPackageThreshold = getConfigThreshold("MULTIPLE_PACKAGES", 2);
        double multiPackageDiscount = getConfigValue("MULTIPLE_PACKAGES", 0.05);
        int frequentClientThreshold = getConfigThreshold("FREQUENT_CLIENT", 3);
        double frequentClientDiscount = getConfigValue("FREQUENT_CLIENT", 0.05);
        int volumeThreshold = getConfigThreshold("VOLUME_DISCOUNT", 4);
        double volumeDiscount = getConfigValue("VOLUME_DISCOUNT", 0.10);
        double maxDiscountLimit = getConfigValue("MAX_DISCOUNT_LIMIT", 0.20);

        if (request.getItems().size() >= multiPackageThreshold) {
            globalDiscount += multiPackageDiscount;
            appliedDiscounts.add(new DiscountDetailDTO("MULTIPLE_PACKAGES", "Discount for purchasing " + request.getItems().size() + " packages together", multiPackageDiscount, 0));
        }

        long paidReservations = reservationRepository.countByUserEmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
            appliedDiscounts.add(new DiscountDetailDTO("FREQUENT_CLIENT", "Frequent client discount (" + paidReservations + " paid reservations)", frequentClientDiscount, 0));
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;
        LocalDate today = LocalDate.now();

        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Passengers must be greater than 0.");
            }

            BundleDTO bundle = getBundleFromMsBundle(item.getBundleId());

            if ("CANCELED".equals(bundle.getStateBundle()) || "EXPIRED".equals(bundle.getStateBundle()) || "SOLD_OUT".equals(bundle.getStateBundle())) {
                throw new IllegalStateException("Cannot reserve the bundle: " + bundle.getNameBundle());
            }

            if (today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("The bundle has already ended: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("Not enough available slots for the bundle: " + bundle.getNameBundle());
            }

            double itemDiscount = globalDiscount;

            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
                if (appliedDiscounts.stream().noneMatch(d -> d.getType().equals("VOLUME_DISCOUNT"))) {
                    appliedDiscounts.add(new DiscountDetailDTO("VOLUME_DISCOUNT", "Group discount for " + item.getPassengers() + "+ passengers", volumeDiscount, 0));
                }
            }

            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null && bundle.getPromoDiscountPercent() != null) {
                if (!today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate())) {
                    itemDiscount += bundle.getPromoDiscountPercent();
                    if (appliedDiscounts.stream().noneMatch(d -> d.getType().equals("PROMOTION") && d.getDescription().contains(bundle.getNameBundle()))) {
                        appliedDiscounts.add(new DiscountDetailDTO("PROMOTION", "Active promotion on " + bundle.getNameBundle(), bundle.getPromoDiscountPercent(), 0));
                    }
                }
            }

            if (itemDiscount > maxDiscountLimit) {
                itemDiscount = maxDiscountLimit;
            }

            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));
            finalPrice = Math.max(0, finalPrice);

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;
        }

        int totalSaved = cartSubtotal - cartFinalTotal;
        if (!appliedDiscounts.isEmpty() && totalSaved > 0) {
            double totalPercentage = appliedDiscounts.stream().mapToDouble(DiscountDetailDTO::getPercentage).sum();
            for (DiscountDetailDTO d : appliedDiscounts) {
                double ratio = (totalPercentage > 0) ? d.getPercentage() / totalPercentage : 0;
                d.setAmount((int) (totalSaved * ratio));
            }
        }

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Quote calculated successfully.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        response.setAppliedDiscounts(appliedDiscounts);
        return response;
    }


    // ---------- Processing Reservations ----------

    @Transactional
    public ReservationResponseDTO processCartReservations(ReservationRequestDTO request, String email) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("The cart cannot be empty.");
        }

        double globalDiscount = 0.0;
        List<DiscountDetailDTO> appliedDiscounts = new ArrayList<>();
        int multiPackageThreshold = getConfigThreshold("MULTIPLE_PACKAGES", 2);
        double multiPackageDiscount = getConfigValue("MULTIPLE_PACKAGES", 0.05);
        int frequentClientThreshold = getConfigThreshold("FREQUENT_CLIENT", 3);
        double frequentClientDiscount = getConfigValue("FREQUENT_CLIENT", 0.05);
        int volumeThreshold = getConfigThreshold("VOLUME_DISCOUNT", 4);
        double volumeDiscount = getConfigValue("VOLUME_DISCOUNT", 0.10);
        double maxDiscountLimit = getConfigValue("MAX_DISCOUNT_LIMIT", 0.20);

        if (request.getItems().size() >= multiPackageThreshold) {
            globalDiscount += multiPackageDiscount;
            appliedDiscounts.add(new DiscountDetailDTO("MULTIPLE_PACKAGES", "Discount for purchasing " + request.getItems().size() + " packages together", multiPackageDiscount, 0));
        }

        long paidReservations = reservationRepository.countByUserEmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
            appliedDiscounts.add(new DiscountDetailDTO("FREQUENT_CLIENT", "Frequent client discount (" + paidReservations + " paid reservations)", frequentClientDiscount, 0));
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;
        List<Long> generatedIds = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Passengers must be greater than 0.");
            }

            BundleDTO bundle = getBundleFromMsBundle(item.getBundleId());

            if ("CANCELED".equals(bundle.getStateBundle()) || "EXPIRED".equals(bundle.getStateBundle()) || "SOLD_OUT".equals(bundle.getStateBundle())) {
                throw new IllegalStateException("Cannot reserve the bundle: " + bundle.getNameBundle());
            }

            if (today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("The bundle has already ended: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("Not enough available slots for the bundle: " + bundle.getNameBundle());
            }

            double itemDiscount = globalDiscount;

            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
                if (appliedDiscounts.stream().noneMatch(d -> d.getType().equals("VOLUME_DISCOUNT"))) {
                    appliedDiscounts.add(new DiscountDetailDTO("VOLUME_DISCOUNT", "Group discount for " + item.getPassengers() + "+ passengers", volumeDiscount, 0));
                }
            }

            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null && bundle.getPromoDiscountPercent() != null) {
                if (!today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate())) {
                    itemDiscount += bundle.getPromoDiscountPercent();
                    if (appliedDiscounts.stream().noneMatch(d -> d.getType().equals("PROMOTION") && d.getDescription().contains(bundle.getNameBundle()))) {
                        appliedDiscounts.add(new DiscountDetailDTO("PROMOTION", "Active promotion on " + bundle.getNameBundle(), bundle.getPromoDiscountPercent(), 0));
                    }
                }
            }

            if (itemDiscount > maxDiscountLimit) {
                itemDiscount = maxDiscountLimit;
            }

            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));
            finalPrice = Math.max(0, finalPrice);

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;

            // Update Bundle Slots in ms-bundle
            decreaseBundleSlots(bundle.getIdBundle(), item.getPassengers());

            // Create and save Reservation
            ReservationEntity newReservation = new ReservationEntity();
            newReservation.setUserEmail(email);
            newReservation.setBundleId(bundle.getIdBundle());
            newReservation.setBundleName(bundle.getNameBundle());
            newReservation.setNumberOfPassengers(item.getPassengers());
            newReservation.setReservationDate(today);
            newReservation.setTotalAmount(finalPrice);
            newReservation.setState(ReservationState.PENDING_PAYMENT);

            ReservationEntity saved = reservationRepository.save(newReservation);
            generatedIds.add(saved.getId());
        }

        int totalSaved = cartSubtotal - cartFinalTotal;
        if (!appliedDiscounts.isEmpty() && totalSaved > 0) {
            double totalPercentage = appliedDiscounts.stream().mapToDouble(DiscountDetailDTO::getPercentage).sum();
            for (DiscountDetailDTO d : appliedDiscounts) {
                double ratio = (totalPercentage > 0) ? d.getPercentage() / totalPercentage : 0;
                d.setAmount((int) (totalSaved * ratio));
            }
        }

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Reservations created successfully.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        response.setGeneratedReservationIds(generatedIds);
        response.setAppliedDiscounts(appliedDiscounts);

        return response;
    }

    // ---------- Background Jobs & Utilities ----------

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cancelExpiredReservations() {
        LocalDate expirationDate = LocalDate.now().minusDays(1);
        List<ReservationEntity> expiredReservations = reservationRepository
                .findByStateAndReservationDateBefore(ReservationState.PENDING_PAYMENT, expirationDate);

        for (ReservationEntity reservation : expiredReservations) {
            reservation.setState(ReservationState.CANCELED);
            
            // Return slots to ms-bundle
            try {
                increaseBundleSlots(reservation.getBundleId(), reservation.getNumberOfPassengers());
            } catch (Exception e) {
                System.err.println("Failed to return slots to ms-bundle for bundle " + reservation.getBundleId());
            }

            reservationRepository.save(reservation);
            System.out.println("Reservation ID " + reservation.getId() + " expired. Slots returned to bundle.");
        }
    }

    public List<ReservationEntity> getUserReservations(String email) {
        return reservationRepository.findByUserEmailOrderByReservationDateDesc(email);
    }

    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    public long countByBundleId(Long bundleId) {
        return reservationRepository.countByBundleId(bundleId);
    }

    @Transactional
    public ReservationEntity updateReservationState(Long id, ReservationState newState) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));

        if (reservation.getState() == ReservationState.CANCELED && newState == ReservationState.CONFIRMED) {
            throw new IllegalStateException("A canceled reservation cannot be confirmed.");
        }
        reservation.setState(newState);
        return reservationRepository.save(reservation);
    }

    public ReservationReceiptDTO generateReceipt(Long reservationId, String callerEmail, boolean isAdmin) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + reservationId));

        if (!isAdmin && !reservation.getUserEmail().equals(callerEmail)) {
            throw new IllegalStateException("You can only access receipts for your own reservations.");
        }

        if (reservation.getState() != ReservationState.CONFIRMED) {
            throw new IllegalStateException("Cannot issue a receipt. The reservation is in state: " + reservation.getState());
        }

        ReservationReceiptDTO receipt = new ReservationReceiptDTO();
        receipt.setReceiptCode("REC-" + reservation.getId() + "-" + reservation.getReservationDate().getYear());
        receipt.setIssueDate(LocalDate.now());
        receipt.setClientEmail(reservation.getUserEmail());
        receipt.setBundleName(reservation.getBundleName());
        
        // Fetch destination from ms-bundle just for the receipt
        try {
            BundleDTO bundle = getBundleFromMsBundle(reservation.getBundleId());
            receipt.setDestination(bundle.getDestinationBundle());
        } catch(Exception e) {
            receipt.setDestination("Unknown Destination");
        }

        receipt.setNumberOfPassengers(reservation.getNumberOfPassengers());
        receipt.setTotalPaid(reservation.getTotalAmount());
        receipt.setStatus("OFFICIALLY PAID");

        return receipt;
    }

    public List<ReservationEntity> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after the end date.");
        }
        return reservationRepository.findSalesByDateRange(startDate, endDate, ReservationState.CANCELED);
    }

    public List<PackageRankingDTO> getPackageRanking(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after the end date.");
        }
        return reservationRepository.findPackageRanking(ReservationState.CANCELED, startDate, endDate);
    }

    private double getConfigValue(String key, double defaultValue) {
        return discountConfigRepository.findByConfigKey(key)
                .map(DiscountConfigEntity::getConfigValue)
                .orElse(defaultValue);
    }

    private int getConfigThreshold(String key, int defaultThreshold) {
        return discountConfigRepository.findByConfigKey(key)
                .map(DiscountConfigEntity::getThreshold)
                .orElse(defaultThreshold);
    }
}
