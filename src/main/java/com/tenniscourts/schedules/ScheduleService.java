package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.exceptions.ParameterTypeMismatchException;
import com.tenniscourts.reservations.ReservationRepository;
import com.tenniscourts.reservations.ReservationStatusEnum;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final ScheduleMapper scheduleMapper;

    private final TennisCourtRepository tennisCourtRepository;

    private final ReservationRepository reservationRepository;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        try {
            TennisCourt tennisCourt = tennisCourtRepository.findById(tennisCourtId)
                    .orElseThrow(() -> new EntityNotFoundException("Tennis Court with guestId " + tennisCourtId + " not found"));
            if (tennisCourt.getPrince() == null) {
                throw new IllegalArgumentException("Tennis court's prince is null");
            }
            if (isTennisCourtReserved(tennisCourtId)) {
                log.warn("Tennis court with ID {} is already reserved. Cannot add schedule.", tennisCourtId);
                throw new AlreadyExistsEntityException("Tennis court is already reserved");
            }
            final Schedule schedule = getSchedule(createScheduleRequestDTO, tennisCourt);
            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("Schedule created successfully: {}", savedSchedule);

            return scheduleMapper.map(savedSchedule);
        } catch (IllegalArgumentException e) {
            throw new ParameterTypeMismatchException("Error adding schedule: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error occurred while adding schedule", e);
            throw e;
        }
    }

    public boolean isTennisCourtReserved(Long tennisCourtId) {
        List<Schedule> schedules = scheduleRepository.findByTennisCourtId(tennisCourtId);
        for (Schedule schedule : schedules) {
            if (reservationRepository.existsBySchedule_TennisCourt_IdAndReservationStatus(schedule, ReservationStatusEnum.READY_TO_PLAY)) {
                return true;
            }
        }
        return false;
    }

    private static Schedule getSchedule(CreateScheduleRequestDTO createScheduleRequestDTO, TennisCourt tennisCourt) {
        LocalDateTime startDateTime = createScheduleRequestDTO.getStartDateTime();
        LocalDateTime endDateTime = createScheduleRequestDTO.getEndDateTime();
        if (startDateTime == null || endDateTime == null || startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Invalid start or end date time");
        }
        Schedule schedule = new Schedule();
        schedule.setTennisCourt(tennisCourt);
        schedule.setStartDateTime(startDateTime);
        schedule.setEndDateTime(endDateTime);
        return schedule;
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            TennisCourt tennisCourt = new TennisCourt();
            List<Schedule> schedules = scheduleRepository.findByStartDateTimeBetweenAndTennisCourt_Id(startDate, endDate, tennisCourt.getId());
            return scheduleMapper.map(schedules);
        } catch (Exception e) {
            log.error("Error occurred while fetching schedules by dates", e);
            throw e;
        }
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));
            return scheduleMapper.map(schedule);
        } catch (Exception e) {
            log.error("Error occurred while fetching schedule by guestId", e);
            throw e;
        }
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }
}
