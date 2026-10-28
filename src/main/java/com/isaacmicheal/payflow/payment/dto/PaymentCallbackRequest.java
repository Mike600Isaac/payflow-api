package com.isaacmicheal.payflow.payment.dto;

import com.isaacmicheal.payflow.domain.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCallbackRequest(
        @NotBlank String reference,
        @NotNull PaymentStatus status) {
}
