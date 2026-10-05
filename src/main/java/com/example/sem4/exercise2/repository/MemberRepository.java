package com.example.sem4.exercise2.repository;

import com.example.sem4.exercise2.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByMemberCode(String memberCode);

    boolean existsByEmail(String email);

    boolean existsByMemberCodeAndIdNot(String memberCode, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * MariaDB 10.4 trên XAMPP không hỗ trợ cú pháp {@code FOR UPDATE OF} của Hibernate.
     * Câu native này khóa đúng dòng độc giả khi kiểm tra quá hạn và giới hạn 5 cuốn.
     */
    @Query(value = "select * from members where id = :id for update", nativeQuery = true)
    Optional<Member> lockById(@Param("id") Long id);
}
