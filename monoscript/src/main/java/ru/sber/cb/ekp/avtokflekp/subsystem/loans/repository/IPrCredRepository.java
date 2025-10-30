package ru.sber.cb.ekp.avtokflekp.subsystem.loans.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity.PrCred;

import java.util.stream.Stream;

@Repository
@Transactional(readOnly = true)
public interface IPrCredRepository extends JpaRepository<PrCred, Long> {

    @Query("select pc from PrCred pc")
    Stream<PrCred> findPrCredByAll();
}