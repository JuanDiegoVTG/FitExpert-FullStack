package com.proyecto.emilite.controller;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        // 1. Por si acaso alguien entra sin sesión
        if (auth == null) {
            return "redirect:/login";
        }

        // A. Buscamos el objeto Usuario completo para tener el ID
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        
        // B. Pasamos el objeto 'usuario' al modelo (Esto arregla tus botones)
        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("nombreUsuario", usuarioActual.getNombres());

        // 2. Extraer datos del usuario actual
        String username = auth.getName();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        boolean isEntrenador = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"));

        // 3. Dato común para todos los dashboards
        model.addAttribute("nombreUsuario", username);

        // 4. EL RUTEO INTELIGENTE A TUS CARPETAS HTML
        if (isAdmin) {
            // Solo al admin le cargamos la base de datos pesada
            model.addAttribute("usuarios", usuarioService.listarTodos());
            // Apunta a: src/main/resources/templates/dashboard/dashboard.html
            return "admin/dashboard/dashboard"; 
            
        } else if (isEntrenador) {
            // Apunta a: src/main/resources/templates/entrenador/dashboard/dashboard.html
            return "entrenador/dashboard/dashboard"; 
            
        } else {
            // Apunta a: src/main/resources/templates/dashboard/dashboard.html
            return "cliente/dashboard/dashboard"; 
        }
    }
}