package steps.business;

import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import steps.SpringIntegrationTest;

@Slf4j
public class ServiceSteps extends SpringIntegrationTest {
    @And("Отправляю POST запрос КонтрактИнфо в сервис Инфо")
    public void sendPostContractDao(){
        log.info(" begin");

    }

}
