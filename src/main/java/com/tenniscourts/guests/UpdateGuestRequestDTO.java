package com.tenniscourts.guests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGuestRequestDTO {
    @NotBlank
    private String name;
}
