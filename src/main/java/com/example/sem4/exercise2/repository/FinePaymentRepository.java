package com.example.sem4.exercise2.repository;

import com.example.sem4.exercise2.entity.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {
}
