package com.example.sem4.exercise2.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrowings", indexes = {
        @Index(name = "idx_borrowings_member_status", columnList = "member_id, status"),
        @Index(name = "idx_borrowings_due_status", columnList = "due_date, status")
})
@Getter
@Setter
@NoArgsConstructor
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate borrowedDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowingStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFineAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidFineAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal waivedFineAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unpaidFineAmount = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowingDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FineWaiver> waivers = new ArrayList<>();

    public void addDetail(BorrowingDetail detail) {
        details.add(detail);
        detail.setBorrowing(this);
    }

    public void addPayment(FinePayment payment) {
        payments.add(payment);
        payment.setBorrowing(this);
    }

    public void addWaiver(FineWaiver waiver) {
        waivers.add(waiver);
        waiver.setBorrowing(this);
    }

    public boolean isFullyReturned() {
        return details != null
                && !details.isEmpty()
                && details.stream().allMatch(detail -> detail.getReturnedQuantity() >= detail.getQuantity());
    }

    public boolean hasUnpaidFine() {
        return unpaidFineAmount != null && unpaidFineAmount.signum() > 0;
    }

    public boolean canReturn() {
        return status == BorrowingStatus.BORROWING
                || status == BorrowingStatus.PARTIALLY_RETURNED
                || status == BorrowingStatus.OVERDUE;
    }

    public boolean canCancel() {
        if (status == BorrowingStatus.CANCELLED
                || status == BorrowingStatus.RETURNED
                || status == BorrowingStatus.FINE_PAID
                || status == BorrowingStatus.FINE_PENDING) {
            return false;
        }
        boolean nothingReturned = details.stream().allMatch(detail -> detail.getReturnedQuantity() == 0);
        boolean noFine = totalFineAmount.signum() == 0
                && paidFineAmount.signum() == 0
                && waivedFineAmount.signum() == 0;
        return nothingReturned && noFine;
    }

    public void recalculateAmounts() {
        BigDecimal total = BigDecimal.ZERO;
        for (BorrowingDetail detail : details) {
            total = total.add(zero(detail.getFineAmount()));
        }
        BigDecimal paid = BigDecimal.ZERO;
        for (FinePayment payment : payments) {
            paid = paid.add(zero(payment.getAmount()));
        }
        BigDecimal waived = BigDecimal.ZERO;
        for (FineWaiver waiver : waivers) {
            waived = waived.add(zero(waiver.getAmount()));
        }
        this.totalFineAmount = total;
        this.paidFineAmount = paid;
        this.waivedFineAmount = waived;
        BigDecimal unpaid = total.subtract(paid).subtract(waived);
        this.unpaidFineAmount = unpaid.signum() < 0 ? BigDecimal.ZERO : unpaid;
    }

    /**
     * Một phiếu chỉ có một trạng thái hiển thị. Ưu tiên: hủy, rồi phiếu đã trả hết
     * (nợ phí / đã trả phí / đã trả), rồi quá hạn, rồi trả một phần, rồi đang mượn.
     */
    public void refreshStatus(LocalDate asOfDate) {
        if (status == BorrowingStatus.CANCELLED) {
            return;
        }
        recalculateAmounts();
        if (isFullyReturned()) {
            if (returnedDate == null) {
                returnedDate = asOfDate;
            }
            if (unpaidFineAmount.signum() > 0) {
                status = BorrowingStatus.FINE_PENDING;
            } else if (totalFineAmount.signum() > 0) {
                status = BorrowingStatus.FINE_PAID;
            } else {
                status = BorrowingStatus.RETURNED;
            }
            return;
        }
        boolean anyReturned = details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0);
        if (dueDate.isBefore(asOfDate)) {
            status = BorrowingStatus.OVERDUE;
        } else if (anyReturned) {
            status = BorrowingStatus.PARTIALLY_RETURNED;
        } else {
            status = BorrowingStatus.BORROWING;
        }
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
