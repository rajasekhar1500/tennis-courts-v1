package com.tenniscourts.guests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGuestRequestDTO {
    @NotBlank
    private String name;
}