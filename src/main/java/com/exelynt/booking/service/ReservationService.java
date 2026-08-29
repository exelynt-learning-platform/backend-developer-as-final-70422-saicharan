package com.exelynt.booking.service;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exelynt.booking.dto.ReservationRequest;
import com.exelynt.booking.dto.ReservationResponse;
import com.exelynt.booking.dto.ReservationUpdateRequest;
import com.exelynt.booking.entity.Reservation;
import com.exelynt.booking.entity.ReservationStatus;
import com.exelynt.booking.entity.Resource;
import com.exelynt.booking.entity.Role;
import com.exelynt.booking.exception.InvalidReservationException;
import com.exelynt.booking.exception.ReservationNotFoundException;
import com.exelynt.booking.exception.ResourceNotFoundException;
import com.exelynt.booking.repository.ReservationRepository;
import com.exelynt.booking.repository.ResourceRepository;
import com.exelynt.booking.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES =
            Set.of("id", "startTime", "endTime", "price", "status", "createdAt");

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationResponse create(ReservationRequest request, UserPrincipal currentUser) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidReservationException("End time must be after start time");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + request.getResourceId()));

        Reservation reservation = Reservation.builder()
                .user(currentUser.getUser())
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .status(ReservationStatus.PENDING)
                .build();

        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse getById(Long id, UserPrincipal currentUser) {
        Reservation reservation = findReservationById(id);
        checkOwnership(reservation, currentUser);
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAll(ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable, UserPrincipal currentUser) {
        validateSort(pageable.getSort());

        Long userId = currentUser.getUser().getRole() == Role.USER ? currentUser.getId() : null;
        Specification<Reservation> spec = buildSpecification(status, minPrice, maxPrice, userId);

        return reservationRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidReservationException("End time must be after start time");
        }

        Reservation reservation = findReservationById(id);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(request.getStatus());

        return toResponse(reservationRepository.save(reservation));
    }

    public void delete(Long id) {
        reservationRepository.delete(findReservationById(id));
    }

    private void checkOwnership(Reservation reservation, UserPrincipal currentUser) {
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getUser().getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to access this reservation");
        }
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException("Invalid sort property: " + order.getProperty());
            }
        }
    }

    private Specification<Reservation> buildSpecification(ReservationStatus status, BigDecimal minPrice,
            BigDecimal maxPrice, Long userId) {
        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        return spec;
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .userEmail(reservation.getUser().getEmail())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .price(reservation.getPrice())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}