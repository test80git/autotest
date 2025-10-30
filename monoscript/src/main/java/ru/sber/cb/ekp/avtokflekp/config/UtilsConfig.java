package ru.sber.cb.ekp.avtokflekp.config;

import com.fasterxml.jackson.databind.ObjectMapper;import com.fasterxml.jackson.dataformat.xml.XmlMapper;import lombok.extern.slf4j.Slf4j;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.http.HttpEntity;import org.springframework.http.client.ClientHttpResponse;import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class UtilsConfig {


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        // логирование
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.info("Request: {} {}, Headers: {}", request.getMethod(), request.getURI(), request.getHeaders());
            ClientHttpResponse response = execution.execute(request, body);
            log.info("Response: {}", response.getStatusCode());
            return response;
        });
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

}
