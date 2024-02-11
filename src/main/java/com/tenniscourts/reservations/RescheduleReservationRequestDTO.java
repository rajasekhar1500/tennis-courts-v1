package com.tenniscourts.reservations;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class RescheduleReservationRequestDTO {
    private Long previousReservationId;
    private Long newScheduleId;
}
