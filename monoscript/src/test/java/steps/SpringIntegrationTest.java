package steps;

import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.sber.cb.ekp.annotation.StepEntity;
import ru.sber.cb.ekp.annotation.StepTransManager;
import ru.sber.cb.ekp.annotation.Steps;
import ru.sber.cb.ekp.avtokflekp.Application;
import ru.sber.cb.ekp.avtokflekp.config.PersistenceLoansAutoConfiguration;
import ru.sber.cb.ekp.avtokflekp.subsystem.loans.repository.IPrCredRepository;
import steps.business.Predicates;

@Steps(
        entityTunes = {
                @StepEntity(
                        packagePath = "ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity",
                        entityManager = "loansEntityManager",
                        transManager = "loansTransactionManager"
                ),
//                @StepEntity(
//                        packagePath = "ru.sber.cb.ekp.avtokflekp.subsystem.zalogs.entity",
//                        entityManager = "zalogsEntityManager",
//                        transManager = "zalogsTransactionManager"
//                ),
        })
@ImportAutoConfiguration(FeignAutoConfiguration.class)
@CucumberContextConfiguration
@SpringBootTest(
        classes = {
                Application.class,
                Predicates.class,
                PersistenceLoansAutoConfiguration.class}
)
public class SpringIntegrationTest {
    @Autowired
    @Qualifier("loansEntityManager")
    protected EntityManager loansEntityManager;

    /*
     * LOANS DB
     * */
    @Autowired
    @StepTransManager("loansTransactionManager")
    protected IPrCredRepository prCredRepository;

}
