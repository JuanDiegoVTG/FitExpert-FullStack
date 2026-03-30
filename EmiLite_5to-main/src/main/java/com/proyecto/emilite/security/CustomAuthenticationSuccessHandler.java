package com.proyecto.emilite.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Obtener los roles del usuario autenticado
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Determinar la URL de redirección según el rol
        String redirectUrl = "/dashboard"; // URL por defecto

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break; // Si es admin, redirige y termina
            } else if (role.equals("ROLE_ENTRENADOR")) {
                redirectUrl = "/entrenador/dashboard";
                break;
            } else if (role.equals("ROLE_CLIENTE")) {
                redirectUrl = "/cliente/dashboard";
                break;
            }
        }

        // Realizar la redirección
        response.sendRedirect(redirectUrl);
    }
}