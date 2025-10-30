package steps.service;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import steps.SpringIntegrationTest;

//import java.util.Map;
//
//import static ru.sber.cb.ekp.utils.Context.getContext;
//import static ru.sber.cb.ekp.utils.Context.setContext;

@Slf4j
public class PrometheusService extends SpringIntegrationTest {


    @And("Сохраняю значение метрики в {context_key} по условию MetricNameAndService")
    public void findMetric(String key, DataTable dataTable) {
//        Map<String, String> dt = dataTable.asMap(String.class, String.class);
//
//        String name = Caster.cast(Context.getSmartContext(dt.get("name")).orElseThrow(), String.class);
//        String metricName = Caster.cast(Context.getSmartContext(dt.get("metricName")).orElseThrow(), String.class);
//        String service = Caster.cast(Context.getSmartContext(dt.get("service")).orElseThrow(), String.class);
//
//        String prometheusResponse = getContext("Metric", String.class).orElseThrow();
//        Double value = PrometheusMetricsParser.parseMetricValueForGetcntrinf(prometheusResponse, name, metricName, service);
//        setContext(key, value);
//        io.qameta.allure.Allure.addAttachment(key, value != null ? value.toString() : "null");
//
//        log.debug("Получил значение метрики: {}", value);
    }
}