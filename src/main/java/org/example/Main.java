package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// ==========================================
// CLASE PRINCIPAL DE LA APLICACIÓN
// ==========================================
@SpringBootApplication
@EnableScheduling
public class Main {

    // ======================================
    // PUNTO DE ENTRADA PRINCIPAL (ENTRADA)
    // ======================================
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}