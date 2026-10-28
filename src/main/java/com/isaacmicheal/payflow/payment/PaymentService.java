package com.isaacmicheal.payflow.payment;

import com.isaacmicheal.payflow.common.NotFoundException;
import com.isaacmicheal.payflow.domain.Merchant;
import com.isaacmicheal.payflow.domain.Payment;
import com.isaacmicheal.payflow.domain.PaymentStatus;
import com.isaacmicheal.payflow.payment.dto.CreatePaymentRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentCallbackRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentResponse;
import com.isaacmicheal.payflow.repository.MerchantRepository;
import com.isaacmicheal.payflow.repository.PaymentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final MerchantRepository merchants;

    public PaymentService(PaymentRepository payments, MerchantRepository merchants) {
        this.payments = payments;
        this.merchants = merchants;
    }

    @Transactional
    public PaymentResponse createPayment(String merchantEmail, CreatePaymentRequest request) {
        Merchant merchant = merchants.findByEmail(merchantEmail)
                .orElseThrow(() -> new NotFoundException("Merchant not found: " + merchantEmail));

        Payment payment = new Payment();
        payment.setReference(generateReference());
        payment.setMerchant(merchant);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().toUpperCase());
        payment.setCustomerEmail(request.customerEmail());
        payment.setDescription(request.description());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());

        return PaymentResponse.from(payments.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listForMerchant(String merchantEmail) {
        Merchant merchant = merchants.findByEmail(merchantEmail)
                .orElseThrow(() -> new NotFoundException("Merchant not found: " + merchantEmail));
        return payments.findByMerchantOrderByCreatedAtDesc(merchant)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getForMerchant(String merchantEmail, String reference) {
        Payment payment = payments.findByReference(reference)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + reference));
        if (!payment.getMerchant().getEmail().equalsIgnoreCase(merchantEmail)) {
            // Do not reveal existence of another merchant's payment.
            throw new NotFoundException("Payment not found: " + reference);
        }
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse handleCallback(PaymentCallbackRequest request) {
        Payment payment = payments.findByReference(request.reference())
                .orElseThrow(() -> new NotFoundException("Payment not found: " + request.reference()));
        payment.setStatus(request.status());
        payment.setUpdatedAt(Instant.now());
        return PaymentResponse.from(payments.save(payment));
    }

    private String generateReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
