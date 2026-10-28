package com.isaacmicheal.payflow.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.isaacmicheal.payflow.domain.Merchant;
import com.isaacmicheal.payflow.domain.Payment;
import com.isaacmicheal.payflow.domain.PaymentStatus;
import com.isaacmicheal.payflow.payment.dto.CreatePaymentRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentCallbackRequest;
import com.isaacmicheal.payflow.payment.dto.PaymentResponse;
import com.isaacmicheal.payflow.repository.MerchantRepository;
import com.isaacmicheal.payflow.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository payments;

    @Mock
    private MerchantRepository merchants;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_startsAsPendingWithGeneratedReference() {
        Merchant merchant = new Merchant();
        merchant.setEmail("merchant@test.com");
        when(merchants.findByEmail("merchant@test.com")).thenReturn(Optional.of(merchant));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePaymentRequest request =
                new CreatePaymentRequest(new BigDecimal("100.00"), "usd", "customer@test.com", "Order 42");

        PaymentResponse response = paymentService.createPayment("merchant@test.com", request);

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals("USD", response.currency());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertNotNull(response.reference());
    }

    @Test
    void handleCallback_updatesPaymentStatus() {
        Merchant merchant = new Merchant();
        merchant.setEmail("merchant@test.com");
        Payment payment = new Payment();
        payment.setReference("PAY-TEST");
        payment.setMerchant(merchant);
        payment.setStatus(PaymentStatus.PENDING);
        when(payments.findByReference("PAY-TEST")).thenReturn(Optional.of(payment));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.handleCallback(
                new PaymentCallbackRequest("PAY-TEST", PaymentStatus.SUCCESS));

        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertNotNull(response.updatedAt());
    }
}
