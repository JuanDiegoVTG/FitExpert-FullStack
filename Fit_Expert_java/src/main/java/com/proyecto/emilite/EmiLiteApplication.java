package com.proyecto.emilite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
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
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(60000);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Interceptor para simular un User-Agent de navegador y evitar el bloqueo de Cloudflare
        restTemplate.getInterceptors().add(userAgentInterceptor());

        return restTemplate;
    }

    private ClientHttpRequestInterceptor userAgentInterceptor() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            request.getHeaders().set("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            request.getHeaders().set("Accept", "application/json");
            return execution.execute(request, body);
        };
    }
}