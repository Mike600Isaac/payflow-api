package com.isaacmicheal.payflow.payment;

import com.isaacmicheal.payflow.common.UnauthorizedException;
import com.isaacmicheal.payflow.payment.dto.CreatePaymentRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentCallbackRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final String webhookSecret;

    public PaymentController(PaymentService paymentService,
                            @Value("${app.webhook.secret}") String webhookSecret) {
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse created = paymentService.createPayment(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<PaymentResponse> list(Authentication authentication) {
        return paymentService.listForMerchant(authentication.getName());
    }

    @GetMapping("/{reference}")
    public PaymentResponse getOne(Authentication authentication, @PathVariable String reference) {
        return paymentService.getForMerchant(authentication.getName(), reference);
    }

    /**
     * Mock payment-gateway webhook. Authenticated by a shared secret header
     * (compared in constant time). In production this would verify an HMAC
     * signature over the raw request body.
     */
    @PostMapping("/callback")
    public PaymentResponse callback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String providedSecret,
            @Valid @RequestBody PaymentCallbackRequest request) {
        if (providedSecret == null || !constantTimeEquals(providedSecret, webhookSecret)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }
        return paymentService.handleCallback(request);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
