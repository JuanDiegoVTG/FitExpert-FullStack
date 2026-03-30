package com.proyecto.emilite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class DashboardController {

    @GetMapping("/dashboard") 
    public String dashboard() {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
           
            return "redirect:/login";
        }

        // Obtener el rol del usuario logueado
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuth -> grantedAuth.getAuthority())
                .orElse("ROLE_ANONYMOUS");

        // Redirigir a la vista específica según el rol
        if (rol.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (rol.equals("ROLE_ENTRENADOR")) {
            return "redirect:/entrenador/dashboard"; 
        } else if (rol.equals("ROLE_CLIENTE")) {
            return "redirect:/cliente/dashboard"; 
        } else {
            
            return "redirect:/error/rol-desconocido"; 
        }
    }

    // ADMIN 
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard/dashboard"; 
    }

    // ENTRENADOR 
    @GetMapping("/entrenador/dashboard")
    public String entrenadorDashboard() {
        return "entrenador/dashboard/dashboard"; 
    }

    // CLIENTE 
    @GetMapping("/cliente/dashboard")
    public String clienteDashboard() {
        return "cliente/dashboard/dashboard"; 
    }
}