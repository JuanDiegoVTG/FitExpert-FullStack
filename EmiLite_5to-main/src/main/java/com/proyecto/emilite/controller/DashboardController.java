package com.proyecto.emilite.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyecto.emilite.model.Progreso;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.ProgresoRepository;
import com.proyecto.emilite.service.UsuarioService;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProgresoRepository progresoRepository;

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

            // 2. Buscamos todos los registros de progreso de este usuario
            List<Progreso> historial = progresoRepository.findByUsuarioOrderByFechaRegistroAsc(usuarioActual);
            
            // 3. Pasamos el historial al modelo para que el JS de la gráfica lo vea
            model.addAttribute("historial", historial);
            
            // Apunta a: src/main/resources/templates/dashboard/dashboard.html
            return "cliente/dashboard/dashboard"; 
        }
    }
}