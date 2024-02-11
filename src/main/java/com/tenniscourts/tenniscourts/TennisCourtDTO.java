package com.tenniscourts.tenniscourts;

import com.tenniscourts.schedules.ScheduleDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TennisCourtDTO {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private BigDecimal prince;

    private List<ScheduleDTO> tennisCourtSchedules;

}
