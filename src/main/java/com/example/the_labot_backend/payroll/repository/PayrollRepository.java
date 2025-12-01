package com.example.the_labot_backend.payroll.repository;


import com.example.the_labot_backend.payroll.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    @Query("""
           SELECT p FROM Payroll p
           WHERE p.site.id = :siteId
           AND FUNCTION('YEAR', p.payDate) = :year
           AND FUNCTION('MONTH', p.payDate) = :month
           """)
    List<Payroll> findBySiteAndMonth(Long siteId, int year, int month);

    // 해당 년,월에 만들어진 payroll이 있는지 확인 개수
    @Query("""
       SELECT COUNT(p) FROM Payroll p
       WHERE p.site.id = :siteId
       AND FUNCTION('YEAR', p.payDate) = :year
       AND FUNCTION('MONTH', p.payDate) = :month
       """)
    Long countBySiteAndYearMonth(Long siteId, int year, int month);

    @Query("""
        SELECT p FROM Payroll p
        WHERE p.worker.id = :workerId
        AND FUNCTION('YEAR', p.payDate) = :year
        AND FUNCTION('MONTH', p.payDate) = :month
    """)
    Optional<Payroll> findByWorkerAndYearAndMonth(
            @Param("workerId") Long workerId,
            @Param("year") int year,
            @Param("month") int month
    );

}
