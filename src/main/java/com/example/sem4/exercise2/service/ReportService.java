package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.OverdueReportRow;
import com.example.sem4.exercise2.entity.Borrowing;
import com.example.sem4.exercise2.entity.BorrowingDetail;
import com.example.sem4.exercise2.entity.BorrowingStatus;
import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.repository.BorrowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BorrowingRepository borrowingRepository;
    private final BorrowingService borrowingService;
    private final FinePolicyService finePolicyService;
    private final FineCalculator fineCalculator;

    @Transactional
    public List<OverdueReportRow> overdueReport() {
        LocalDate today = LocalDate.now();
        borrowingService.refreshOverdue(today);
        FinePolicy policy = finePolicyService.findActiveOrNull();
        List<OverdueReportRow> rows = new ArrayList<>();
        for (Borrowing borrowing : borrowingRepository.findDetailedByStatus(BorrowingStatus.OVERDUE)) {
            int unreturned = 0;
            BigDecimal estimated = BigDecimal.ZERO;
            for (BorrowingDetail detail : borrowing.getDetails()) {
                int remaining = detail.getRemainingQuantity();
                if (remaining <= 0) {
                    continue;
                }
                unreturned += remaining;
                if (policy != null) {
                    estimated = estimated.add(fineCalculator.quote(
                            borrowing.getDueDate(), today, remaining, detail.getFineAmount(), policy).amount());
                }
            }
            long lateDays = Math.max(0, ChronoUnit.DAYS.between(borrowing.getDueDate(), today));
            rows.add(new OverdueReportRow(
                    borrowing.getId(),
                    borrowing.getMember().getMemberCode(),
                    borrowing.getMember().getFullName(),
                    borrowing.getDueDate(),
                    lateDays,
                    unreturned,
                    estimated,
                    borrowing.getUnpaidFineAmount()
            ));
        }
        rows.sort(Comparator.comparingLong(OverdueReportRow::lateDays).reversed());
        return rows;
    }
}
