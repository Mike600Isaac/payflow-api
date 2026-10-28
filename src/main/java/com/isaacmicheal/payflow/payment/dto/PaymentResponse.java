package com.isaacmicheal.payflow.payment.dto;

import com.isaacmicheal.payflow.domain.Payment;
import com.isaacmicheal.payflow.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String reference,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String customerEmail,
        String description,
        Instant createdAt,
        Instant updatedAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getReference(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getCustomerEmail(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
