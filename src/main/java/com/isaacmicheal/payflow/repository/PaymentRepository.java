package com.isaacmicheal.payflow.repository;

import com.isaacmicheal.payflow.domain.Merchant;
import com.isaacmicheal.payflow.domain.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReference(String reference);

    List<Payment> findByMerchantOrderByCreatedAtDesc(Merchant merchant);
}
