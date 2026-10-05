package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.FinePolicyForm;
import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.repository.FinePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinePolicyService {

    private final FinePolicyRepository finePolicyRepository;

    @Transactional(readOnly = true)
    public List<FinePolicy> findAll() {
        return finePolicyRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public FinePolicy requireActive() {
        return finePolicyRepository.findFirstByActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new BusinessException("Chưa có chính sách phí phạt đang áp dụng"));
    }

    @Transactional(readOnly = true)
    public FinePolicy findActiveOrNull() {
        return finePolicyRepository.findFirstByActiveTrueOrderByIdDesc().orElse(null);
    }

    /**
     * Chính sách mới được lưu thành bản ghi riêng và trở thành bản duy nhất đang dùng.
     * Phiếu đã tính phí trước đó giữ nguyên số tiền đã chốt lúc trả sách.
     */
    @Transactional(rollbackFor = Exception.class)
    public FinePolicy publish(FinePolicyForm form) {
        finePolicyRepository.deactivateAll();
        FinePolicy policy = new FinePolicy();
        policy.setDailyFineAmount(form.getDailyFineAmount());
        policy.setGraceDays(form.getGraceDays());
        BigDecimal max = form.getMaxFineAmount();
        policy.setMaxFineAmount(max != null && max.signum() > 0 ? max : null);
        policy.setActive(true);
        policy.setCreatedAt(LocalDate.now());
        return finePolicyRepository.save(policy);
    }
}
