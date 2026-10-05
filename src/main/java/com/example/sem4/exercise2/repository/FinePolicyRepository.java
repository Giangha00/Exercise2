package com.example.sem4.exercise2.repository;

import com.example.sem4.exercise2.entity.FinePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinePolicyRepository extends JpaRepository<FinePolicy, Long> {

    Optional<FinePolicy> findFirstByActiveTrueOrderByIdDesc();

    List<FinePolicy> findAllByOrderByIdDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update FinePolicy p set p.active = false where p.active = true")
    int deactivateAll();
}
