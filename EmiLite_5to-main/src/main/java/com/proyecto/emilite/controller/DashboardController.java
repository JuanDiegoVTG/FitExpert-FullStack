package com.proyecto.emilite.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyecto.emilite.model.Notificacion;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.NotificacionRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.ContratoService; 
import com.proyecto.emilite.service.UsuarioService;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionRepository notificacionRepository; 

    @Autowired
    private ContratoService contratoService; 

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        // 1. Validación de sesión
        if (auth == null) {
            return "redirect:/login";
        }

        // 2. Extraer datos del usuario actual
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(username).orElse(null);
        
        if (usuario == null) return "redirect:/login";

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        boolean isEntrenador = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"));

        // 3. DATOS COMUNES (Notificaciones de Johan)
        // Traemos las notificaciones no leídas para la campana de Karol
        List<Notificacion> misNotis = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuario.getId());
        
        model.addAttribute("nombreUsuario", username);
        model.addAttribute("usuario", usuario); // Mandamos el objeto completo para el ID en JS
        model.addAttribute("notificaciones", misNotis);
        model.addAttribute("totalNotificaciones", misNotis.size());

        // 4. LÓGICA POR ROLES
        if (isAdmin) {
            model.addAttribute("usuarios", usuarioService.listarTodos());
            return "admin/dashboard/dashboard"; 
            
        } else if (isEntrenador) {
            // Dashboard para entrenadores
            return "entrenador/dashboard/dashboard"; 
            
        } else {
            // DASHBOARD CLIENTE 
            // Verificamos si tiene contrato activo para mostrar la medalla PRO
            boolean tieneContrato = contratoService.tieneEntrenadorActivo(usuario);
            model.addAttribute("tieneContrato", tieneContrato);
            
            return "cliente/dashboard/dashboard"; 
        }
    }
}