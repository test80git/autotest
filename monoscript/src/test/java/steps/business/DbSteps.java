package steps.business;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
//import ru.sber.cb.ekp.avtokflekp.service.GetSumDebt.dto.response.SumDebtRs;
//import ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity.ArcSchPrc;
//import ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity.PrCred;
//import ru.sber.cb.ekp.avtokflekp.subsystem.zalogs.entity.AutoLoanStore;
//import ru.sber.cb.ekp.utils.Caster;
//import ru.sber.cb.ekp.utils.Context;
//import ru.sber.cb.ekp.utils.PredicateContext;
import steps.SpringIntegrationTest;

//import java.math.BigDecimal;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import static ru.sber.cb.ekp.utils.Context.getSmartContext;
//import static ru.sber.cb.ekp.utils.Context.setContext;

@Slf4j
public class DbSteps extends SpringIntegrationTest {


    @And("Поиск PrCred по условию SumDebtZero")
    @Transactional(
            value = "loansTransactionManager",
            readOnly = true
    )
    public void findPrCredBySumDebtZero() {
//        try (var stream = prCredRepository.findPrCredByStatusAndKind(
//                "WORK", "2.10_.%"
//        )) {
//            stream.anyMatch(cred -> {
//                Context.setContext("PrCred", cred);
//                sumDebtRqGenerator.generate();
//                sumDebtDao.post();
//                if (Context.getContext("SumDebtRs", SumDebtRs.class).orElseThrow().getResult().compareTo(BigDecimal.ZERO) == 0) {
//                    log.debug("Найден {}: {}", "PrCred", cred);
//                    io.qameta.allure.Allure.addAttachment("PrCred", cred.toString());
//                    return true;
//                }
//                return false;
//            });
//        }
    }

    @And("Поиск PrCred по условию StatusAndKindAndSumDebt")
    @Transactional(
            value = "loansTransactionManager",
            readOnly = true
    )
    public void findPrCredBySumDebtSumDebt(DataTable dataTable) {
//        Map<String, String> dt = dataTable.asMap(String.class, String.class);
//
//        String status = Caster.cast(Context.getSmartContext(dt.get("status")).orElseThrow(), String.class);
//        String kind = Caster.cast(Context.getSmartContext(dt.get("kind")).orElseThrow(), String.class);
//        String saldo = Caster.cast(Context.getSmartContext(dt.get("saldo")).orElseThrow(), String.class);
//        BigDecimal expectedSaldo = new BigDecimal(saldo);
//        try (var stream = prCredRepository.findPrCredByStatusAndKind(status, kind)) {
//            stream.anyMatch(cred -> {
//                Context.setContext("PrCred", cred);
//                setContext("SumDebtRq", sumDebtRqGenerator.generate());
//                sumDebtDao.post();
//                if (Context.getContext("SumDebtRs", SumDebtRs.class)
//                        .orElseThrow()
//                        .getResult()
//                        .compareTo(expectedSaldo) == 0) {
//                    log.debug("Найден {}: {}", "PrCred", cred);
//                    io.qameta.allure.Allure.addAttachment("PrCred", cred.toString());
//                    return true;
//                }
//                return false;
//            });
//        }
    }

    @Given("Ищу AutoLoanStore с PrCred")
    @Transactional(transactionManager = "zalogsTransactionManager", readOnly = true)
    public void findAutoLoanStoresWithPrCred() {
//        log.info("Начало поиска первого AutoLoanStore с PrCred");
//
//        Optional<AutoLoanStore> result = autoLoanStoreRepository.findAutoLoanStoreByIdEkpLoanIsNotNull()
//                .filter(als -> {
//                    try {
//                        Long id = Long.parseLong(als.getIdEkpLoan());
//                        return prCredRepository.findPrCredById(id).isPresent();
//                    } catch (NumberFormatException e) {
//                        log.debug("Пропускаем нечисловой ID: {}", als.getIdEkpLoan());
//                        return false;
//                    }
//                })
//                .findFirst();
//
//        if (result.isPresent()) {
//            log.info("Найден первый AutoLoanStore с PrCred: AutoLoanStore={}", result.get());
//            setContext("AutoLoanStore", result.get());
//        } else {
//            log.info("Не найдено ни одного AutoLoanStore с PrCred");
//            setContext("AutoLoanStore", null);
//        }
    }

    @Given("Ищу AutoLoanStore без PrCred")
    @Transactional(transactionManager = "zalogsTransactionManager", readOnly = true)
    public void findAutoLoanStoresWithoutPrCred() {
//        log.info("Начало поиска первого AutoLoanStore без PrCred");
//
//        Optional<AutoLoanStore> result = autoLoanStoreRepository.findAutoLoanStoreByIdEkpLoanIsNotNull()
//                .filter(als -> {
//                    try {
//                        Long id = Long.parseLong(als.getIdEkpLoan());
//                        return !prCredRepository.findPrCredById(id).isPresent();
//                    } catch (NumberFormatException e) {
//                        log.debug("Пропускаем нечисловой ID: {}", als.getIdEkpLoan());
//                        return false;
//                    }
//                })
//                .findFirst();
//
//        if (result.isPresent()) {
//            log.info("Найден первый AutoLoanStore без PrCred: AutoLoanStore={}", result.get());
//            setContext("AutoLoanStore", result.get());
//        } else {
//            log.info("Не найдено ни одного AutoLoanStore без PrCred");
//            setContext("AutoLoanStore", null);
//        }
    }

    @Given("Ищу PrCred по условию SmsPostZalog")
    @Transactional(transactionManager = "loansTransactionManager", readOnly = true)
    public void findPrCredSmsPostZalog(DataTable dataTable) {
//        Map<String, String> dt = dataTable.asMap(String.class, String.class);
//        String status = Caster.cast(Context.getSmartContext(dt.get("status")).orElseThrow(), String.class);
//        PrCred result = prCredRepository.findBySmsPostZalog(status)
//                .filter(prCred -> {
//                    Optional<ArcSchPrc> oPromo = arcSchPrcRepository.findArcSchPrcByPrCredOrderByDateBegAsc(prCred).findAny();
//                    return oPromo.isPresent() &&
//                            oPromo.get().getDateEnd() != null &&
//                            autoLoanStoreRepository.findAutoLoanStoreByIdEkpLoan(prCred.getId().toString()).isEmpty();
//                }).findFirst().get();
//        log.info("{}", result);
//        setContext("PrCred", result);
    }

}
