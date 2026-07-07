package com.proyecto.emilite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EmiLiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmiLiteApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15s para establecer conexión
        factory.setReadTimeout(60000);    // 60s para esperar respuesta (cubre el cold start de Flask)
        return new RestTemplate(factory);
    }
}