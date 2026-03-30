package com.proyecto.emilite.hash;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneradorHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String contraseñaOriginal = "admin123"; // Cambia esto por la contraseña que quieras usar
        String hash = encoder.encode(contraseñaOriginal);
        System.out.println("Contraseña original: " + contraseñaOriginal);
        System.out.println("Hash BCrypt: " + hash);
    }
}
