package steps.business;

import org.springframework.stereotype.Component;
//import ru.sber.cb.ekp.utils.Context;
import ru.sber.cb.ekp.utils.PredicateContext;
import steps.SpringIntegrationTest;

//import static ru.sber.cb.ekp.utils.Context.setContext;

@Component
public class Predicates extends SpringIntegrationTest {
    public Predicates() {
        PredicateContext.register("zeroSaldo", (loanId) -> zeroSaldo((Long) loanId), Long.class);
        PredicateContext.register("notZeroSaldo", (loanId) -> !zeroSaldo((Long) loanId), Long.class);
        PredicateContext.register("hasPZCarPrc", (loanId) -> hasPZCarPrc((Long) loanId), Long.class);
   }


    public boolean zeroSaldo(Long loanId) {
//        SumDebtRq tmp = sumDebtRqGenerator.generate();
//        tmp.getIdCred().setId(loanId);
//        setContext("SumDebtRq", tmp);
//        sumDebtDao.post();
//        return Context.getContext("SumDebtRs", SumDebtRs.class)
//                .orElseThrow(() -> new RuntimeException("SumDebtRs not found"))
//                .getResult().compareTo(BigDecimal.ZERO) == 0;
        return false;
    }

    public boolean hasALS(String loanId) {
//        return autoLoanStoreRepository.findAutoLoanStoreByIdEkpLoan(loanId).isPresent();
        return false;
    }

    public boolean hasPZCarPrc(Long idEkpLoan) {
//        return prCredRepository.findPrCredByIdWithCarPrcAndDecl(idEkpLoan)
//                .map(prCred -> {
//                    setContext("PrCred", prCred);
//                    return true;
//                })
//                .orElse(false);
        return false;
    }

}