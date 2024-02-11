package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.tenniscourts.TennisCourt;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final ScheduleRepository scheduleRepository;

    private final GuestRepository guestRepository;



    private static final BigDecimal RESERVATION_DEPOSIT_AMOUNT = BigDecimal.TEN;

    @Transactional
    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        Schedule schedule = getScheduleById(createReservationRequestDTO.getScheduleId());

        if (!isScheduleAvailable(schedule)) {
            throw new IllegalArgumentException("The selected schedule is not available for reservation");
        }

        Reservation reservation = createReservation(createReservationRequestDTO, schedule);
        log.info("Reservation created successfully: {}", reservation);

        return reservationMapper.map(reservation);
    }

    private Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with guestId: " + scheduleId));
    }


    private boolean isScheduleAvailable(Schedule schedule) {
        return reservationRepository.countByScheduleAndReservationStatus(schedule, ReservationStatusEnum.READY_TO_PLAY) == 0;
    }

    public Reservation createReservation(CreateReservationRequestDTO createReservationRequestDTO, Schedule schedule) {
        Long guestId = createReservationRequestDTO.getGuestId();
        boolean hasActiveReservation = reservationRepository.existsByGuestIdAndReservationStatus(guestId, ReservationStatusEnum.READY_TO_PLAY);

        if (hasActiveReservation) {
            throw new IllegalArgumentException("The guest already has an active reservation.");
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found with guestId: " + guestId));

        TennisCourt tennisCourt = schedule.getTennisCourt();
        tennisCourt.setPrince(BigDecimal.TEN);
        if (tennisCourt == null || tennisCourt.getPrince() == null) {
            throw new IllegalArgumentException("Tennis court or its prince is null");
        }
        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setDepositAmount(RESERVATION_DEPOSIT_AMOUNT);
        reservation.setSchedule(schedule);
        reservation.setPrince(tennisCourt.getPrince());

        reservation.setReservationStatus(ReservationStatusEnum.READY_TO_PLAY);
        reservationRepository.save(reservation);
        return reservation;
    }

    @Transactional(readOnly = true)
    public ReservationDTO findReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        return reservationMapper.map(reservation);
    }

    private Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));
    }

    @Transactional
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));
        validateCancellation(reservation);
        BigDecimal refundValue = getRefundValue(reservation);
        reservation.setReservationStatus(ReservationStatusEnum.CANCELLED);
        reservation.setPrince(reservation.getPrince().subtract(refundValue));
        reservation.setRefundValue(refundValue);
        reservationRepository.save(reservation);
        return reservationMapper.map(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatusEnum.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }
        if (reservation.getSchedule().getStartDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule past reservations.");
        }
    }

    private BigDecimal getRefundValue(Reservation reservation) {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());
        return hours >= 24 ? reservation.getPrince() : BigDecimal.TEN;
    }

    @Transactional
    public ReservationDTO rescheduleReservation(Long previousReservationId, Long newScheduleId) {
        Reservation previousReservation = reservationRepository.findById(previousReservationId)
                .orElseThrow(() -> new EntityNotFoundException("Previous reservation not found."));

        cancelReservation(previousReservationId);

        Schedule newSchedule = scheduleRepository.findById(newScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("New schedule not found."));
        previousReservation.setReservationStatus(ReservationStatusEnum.RESCHEDULED);
        previousReservation.setSchedule(newSchedule);
        reservationRepository.save(previousReservation);

        return reservationMapper.map(previousReservation);
    }
    @Transactional
    public ReservationDTO rescheduleReservationFor24Hours(Long previousReservationId, Long newScheduleId) {

        Reservation previousReservation = reservationRepository.findById(previousReservationId)
                .orElseThrow(() -> new EntityNotFoundException("Previous reservation not found."));

        cancelReservationFor24Hours(previousReservationId);

        Schedule newSchedule = scheduleRepository.findById(newScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("New schedule not found."));

        previousReservation.setReservationStatus(ReservationStatusEnum.RESCHEDULED);
        previousReservation.setSchedule(newSchedule);
        reservationRepository.save(previousReservation);

        return reservationMapper.map(previousReservation);
    }

    @Transactional
    public ReservationDTO cancelReservationFor24Hours(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with ID: " + reservationId));
        validateCancellation(reservation);
        if (isMoreThan24HoursInAdvance(reservation.getSchedule().getStartDateTime())) {
            refundReservationDeposit(reservation);
        }

        reservation.setReservationStatus(ReservationStatusEnum.CANCELLED);
        reservation.setPrince(BigDecimal.ZERO);
        reservationRepository.save(reservation);
        return reservationMapper.map(reservation);
    }

    private boolean isMoreThan24HoursInAdvance(LocalDateTime reservationStartTime) {
        long hoursUntilReservation = ChronoUnit.HOURS.between(LocalDateTime.now(), reservationStartTime);
        return hoursUntilReservation > 24;
    }

    public void refundReservationDeposit(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatusEnum.DEPOSIT_CHARGED) {
            throw new IllegalStateException("Cannot refund deposit for reservation with status: " + reservation.getReservationStatus());
        }

        LocalDateTime reservationStartTime = reservation.getSchedule().getStartDateTime();
        long hoursUntilReservation = ChronoUnit.HOURS.between(LocalDateTime.now(), reservationStartTime);

        if (hoursUntilReservation > 24) {
            BigDecimal depositAmount = BigDecimal.TEN;
            reservation.setDepositAmount(reservation.getRefundValue());
            reservation.setReservationStatus(ReservationStatusEnum.REFUNDED);
            reservationRepository.save(reservation);
        } else {
            throw new IllegalStateException("Cannot refund deposit as reservation was cancelled or rescheduled less than 24 hours in advance.");
        }
    }
    public void refund(BigDecimal amount) {
        System.out.println("Refunding $" + amount);
        System.out.println("Refund successful!");
    }
}
