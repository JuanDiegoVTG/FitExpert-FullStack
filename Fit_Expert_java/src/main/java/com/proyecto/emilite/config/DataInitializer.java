package com.proyecto.emilite.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Vaciado para que no choque con las validaciones de apellidos
        System.out.println("🌱 Inicializador saltado con éxito.");
    }
}