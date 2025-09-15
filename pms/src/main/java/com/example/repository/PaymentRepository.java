package com.example.repository;


import com.example.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
 Payment findByTransactionId(String transactionId);

    List<Payment> findByStatus(String status);
}