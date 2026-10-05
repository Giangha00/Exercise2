package com.example.sem4.exercise2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fine_policies")
@Getter
@Setter
@NoArgsConstructor
public class FinePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyFineAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxFineAmount;

    @Column(nullable = false)
    private Integer graceDays;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDate createdAt;
}
