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
import com.proyecto.emilite.repository.PagoRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.UsuarioService;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        // 1. Seguridad: Verificar sesión activa
        if (auth == null) {
            return "redirect:/login";
        }

        // Obtener los datos del usuario que inició sesión
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        
        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("usuarioId", usuarioActual.getId()); // Clave para scripts de JS internos

        // Validar Roles/Autoridades del usuario logueado
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isEntrenador = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"));

        // 2. ENRUTAMIENTO Y ASIGNACIÓN DE DATOS SEGÚN EL ROL
        if (isAdmin) {
            // === DATOS EN VIVO PARA LAS TARJETAS DEL ADMINISTRADOR ===
            
            // Tarjeta 1: Total de usuarios registrados (MySQL)
            long totalUsuarios = usuarioRepository.count();
            model.addAttribute("totalUsuarios", totalUsuarios);

            // Tarjeta 2: Rutinas IA o registros de progresos totales
            long totalRutinas = progresoRepository.count();
            model.addAttribute("totalRutinas", totalRutinas);

            // Tarjeta 3: Suma total de los montos de la tabla Pagos
            Double totalVentas = pagoRepository.sumarTotalVentas();
            if (totalVentas == null) {
                totalVentas = 0.0;
            }
            model.addAttribute("totalVentas", totalVentas);

            // Tarjeta 4: Valor estático o calculado del Uptime del servidor
            model.addAttribute("uptime", 99); 

            // Datos requeridos para la tabla de gestión interna de usuarios
            model.addAttribute("usuarios", usuarioService.listarTodos());
            
            return "admin/dashboard/dashboard"; 

        } else if (isEntrenador) {
            return "entrenador/dashboard/dashboard"; 

        } else {
            // --- LÓGICA EXCLUSIVA PARA EL ROL CLIENTE ---
            if (usuarioActual.getEntrenador() != null) {
                String nombreCompleto = usuarioActual.getEntrenador().getNombres() + " " + usuarioActual.getEntrenador().getApellidos();
                model.addAttribute("nombreEntrenador", nombreCompleto);
            } else {
                model.addAttribute("nombreEntrenador", "Sin entrenador asignado");
            }

            // Historial físico ordenado de manera ascendente para renderizar las gráficas
            List<Progreso> historial = progresoRepository.findByUsuarioOrderByFechaRegistroAsc(usuarioActual);
            model.addAttribute("historial", historial);
            
            return "cliente/dashboard/dashboard"; 
        }
    }
}