package com.tenniscourts.reservations;

import com.tenniscourts.schedules.Schedule;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    int countByScheduleAndReservationStatus(Schedule schedule, ReservationStatusEnum reservationStatusEnum);

    boolean existsByGuestIdAndReservationStatus(Long guestId, ReservationStatusEnum reservationStatusEnum);

    boolean existsBySchedule_TennisCourt_IdAndReservationStatus(Schedule tennisCourtId, ReservationStatusEnum reservationStatus);
}
