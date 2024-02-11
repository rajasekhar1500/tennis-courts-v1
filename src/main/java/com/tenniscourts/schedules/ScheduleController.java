package com.tenniscourts.schedules;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@Api(tags = "Schedule Management")
@AllArgsConstructor
public class ScheduleController extends BaseRestController {

    private final ScheduleService scheduleService;

    @PostMapping("/add")
    @ApiOperation("Add a new schedule")
    public ResponseEntity<Void> addScheduleTennisCourt(@RequestBody CreateScheduleRequestDTO createScheduleRequestDTO) {
        return ResponseEntity.created(locationByEntity(scheduleService.addSchedule(createScheduleRequestDTO.getTennisCourtId(), createScheduleRequestDTO).getId())).build();
    }

    @GetMapping("/find")
    @ApiOperation("Find schedules by dates")
    public ResponseEntity<List<ScheduleDTO>> findSchedulesByDates(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate) {
        return ResponseEntity.ok(scheduleService.findSchedulesByDates(LocalDateTime.of(startDate, LocalTime.of(0, 0)), LocalDateTime.of(endDate, LocalTime.of(23, 59))));
    }

    @GetMapping("/{scheduleId}")
    @ApiOperation("Get a schedule by ID")
    public ResponseEntity<ScheduleDTO> findByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findSchedule(scheduleId));
    }
}
