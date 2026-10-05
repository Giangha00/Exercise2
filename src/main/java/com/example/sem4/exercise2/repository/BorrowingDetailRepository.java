package com.example.sem4.exercise2.repository;

import com.example.sem4.exercise2.entity.BorrowingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {

    @Query("""
            select coalesce(sum(d.quantity - d.returnedQuantity), 0)
            from BorrowingDetail d
            where d.borrowing.member.id = :memberId
              and d.borrowing.status in (
                com.example.sem4.exercise2.entity.BorrowingStatus.BORROWING,
                com.example.sem4.exercise2.entity.BorrowingStatus.PARTIALLY_RETURNED,
                com.example.sem4.exercise2.entity.BorrowingStatus.OVERDUE
              )
            """)
    long sumOutstandingCopies(@Param("memberId") Long memberId);
}
