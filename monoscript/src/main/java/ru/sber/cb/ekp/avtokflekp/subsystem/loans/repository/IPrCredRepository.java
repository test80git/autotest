package ru.sber.cb.ekp.avtokflekp.subsystem.loans.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity.PrCred;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Transactional(readOnly = true)
public interface IPrCredRepository extends JpaRepository<PrCred, Long> {

    @Query("select pc from PrCred pc")
    Stream<PrCred> findPrCredByAll();

    @Query("select pc from PrCred pc WHERE pc.summaDog > :sumdog order by pc.summaDog desc ")
    Stream<PrCred> findPrCredBySumDogMoreOrderByDesc(@Param("sumdog") BigDecimal sumdog);

    @Query("select pc from PrCred pc WHERE pc.summaDog < :sumdog order by pc.summaDog desc ")
    Stream<PrCred> findPrCredBySumDogLessOrderByDesc(@Param("sumdog") BigDecimal sumdog);

    Optional<PrCred> findPrCredById(Long id);
}