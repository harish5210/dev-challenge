package com.dws.challenge.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    @NotNull
    @NotBlank(message = "AccountFrom cannot be null or blank.")
    private String accountFrom;

    @NotNull
    @NotBlank(message = "AccountTo cannot be null or blank.")
    private String accountTo;

    @NotNull
    @Min(value = 0, message = "Amount Transfer must be positive.")
    private BigDecimal amount;
}

