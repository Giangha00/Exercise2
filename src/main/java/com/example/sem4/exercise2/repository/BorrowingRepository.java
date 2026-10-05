package com.example.sem4.exercise2.repository;

import com.example.sem4.exercise2.entity.Borrowing;
import com.example.sem4.exercise2.entity.BorrowingStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long>, JpaSpecificationExecutor<Borrowing> {

    List<Borrowing> findByStatusInAndDueDateBefore(Collection<BorrowingStatus> statuses, LocalDate date);

    @EntityGraph(attributePaths = {"member", "details", "details.book"})
    @Query("select b from Borrowing b where b.status = :status")
    List<Borrowing> findDetailedByStatus(@Param("status") BorrowingStatus status);

    @Query("""
            select count(b) from Borrowing b
            where b.member.id = :memberId
              and b.status <> com.example.sem4.exercise2.entity.BorrowingStatus.CANCELLED
              and (b.status = com.example.sem4.exercise2.entity.BorrowingStatus.OVERDUE
                   or b.unpaidFineAmount > 0)
            """)
    long countBlocking(@Param("memberId") Long memberId);

    @Override
    @EntityGraph(attributePaths = "member")
    Page<Borrowing> findAll(Specification<Borrowing> spec, Pageable pageable);
}
