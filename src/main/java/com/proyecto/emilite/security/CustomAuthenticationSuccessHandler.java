package com.proyecto.emilite.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // ¡LA MAGIA DEL DASHBOARD UNIFICADO!
        // Como Thymeleaf ahora se encarga de mostrar/ocultar cosas según el rol,
        // simplemente mandamos a todos los usuarios autenticados a la ruta principal.
        response.sendRedirect("/dashboard");
    }
}