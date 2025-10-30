package ru.sber.cb.ekp.avtokflekp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UrlsConfig {
    @Bean
    public String loansJappUrl(){
        return "http://localhost:8080/loans";
    }
}
