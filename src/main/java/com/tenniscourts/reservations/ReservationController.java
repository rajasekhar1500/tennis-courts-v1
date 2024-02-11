package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@Api(tags = "Reservation Management")
@AllArgsConstructor
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @PostMapping("/book")
    @ApiOperation("Book a reservation")
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }
    @GetMapping("/find/{reservationId}")
    @ApiOperation("Get a reservation by ID")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }
    @PostMapping("/{reservationId}/cancel")
    @ApiOperation("Cancel a reservation")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }
    @PostMapping("/{reservationId}/reschedule/{scheduleId}")
    @ApiOperation("Reschedule a reservation")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId));
    }
    @DeleteMapping("/cancel/{reservationId}")
    @ApiOperation(value = "Cancel a reservation for 24 hours")
    public ResponseEntity<?> cancelReservationFor24Hours(@PathVariable Long reservationId) {
        ReservationDTO canceledReservation = reservationService.cancelReservationFor24Hours(reservationId);
        return ResponseEntity.ok(canceledReservation);
    }
    @PostMapping("/reschedule")
    @ApiOperation(value = "Reschedule a reservation for 24 hours")
    public ResponseEntity<ReservationDTO> rescheduleReservationFor24Hours(@RequestBody RescheduleReservationRequestDTO requestDTO) {
        ReservationDTO reservationDTO = reservationService.rescheduleReservationFor24Hours(requestDTO.getPreviousReservationId(), requestDTO.getNewScheduleId());
        return ResponseEntity.ok(reservationDTO);
    }
}
