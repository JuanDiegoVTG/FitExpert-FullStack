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
    // 1. Seguridad: Verificar sesión
    if (auth == null) {
        return "redirect:/login";
    }

    // A. Buscamos el objeto Usuario completo (el cliente)
    Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
    
    // B. Pasamos el objeto 'usuario' al modelo (Esto ayuda en la vista principal)
    model.addAttribute("usuario", usuarioActual);
    model.addAttribute("usuarioId", usuarioActual.getId()); // ¡IMPORTANTE para tu JS!

    // C. Datos de autoridad para decidir qué dashboard mostrar
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isEntrenador = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"));

    // 2. LÓGICA DE RUTEO (Aquí decidimos qué devolver)
    if (isAdmin) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/dashboard/dashboard"; 

    } else if (isEntrenador) {
        return "entrenador/dashboard/dashboard"; 

    } else {
        // --- 3. LÓGICA ESPECIAL PARA EL CLIENTE ---
        
        // A. Nombre del Entrenador (Para el sidebar)
        if (usuarioActual.getEntrenador() != null) {
            String nombreCompleto = usuarioActual.getEntrenador().getNombres() + " " + usuarioActual.getEntrenador().getApellidos();
            model.addAttribute("nombreEntrenador", nombreCompleto);
        } else {
            model.addAttribute("nombreEntrenador", "Sin entrenador asignado");
        }

        // C. Historial para gráficas (Lo que ya tenías)
        List<Progreso> historial = progresoRepository.findByUsuarioOrderByFechaRegistroAsc(usuarioActual);
        model.addAttribute("historial", historial);
        
        // Retorno final
        return "cliente/dashboard/dashboard"; 
        }
    }
}