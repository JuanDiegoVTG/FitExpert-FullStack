package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.emilite.model.Notificacion;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.NotificacionRepository;
import com.proyecto.emilite.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * 1. CONTEO DE NO LEÍDAS
     * Se usa para el numerito rojo de la campana.
     * Ruta: GET /api/notificaciones/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> obtenerContador(Authentication auth) {
        // auth.getName() aquí es "JuanDiegoVTG"
        String loginName = auth.getName();
        
        Usuario usuario = usuarioRepository.findByUserName(loginName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        long count = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
        return ResponseEntity.ok(count);
    }

    /**
     * 2. LISTA PARA EL DESPLEGABLE
     * Trae las últimas notificaciones para mostrar en el dropdown.
     * Ruta: GET /api/notificaciones/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<Notificacion>> listarMisNotificaciones(Authentication auth) {
        String loginName = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(loginName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        // Usamos el método que ordena por fecha para que no salgan los NULL primero

        List<Notificacion> notis = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuario.getId());
        return ResponseEntity.ok(notis);
    }

    /**
     * 3. MARCAR UNA COMO LEÍDA
     * Se activa cuando el usuario hace clic en una notificación específica.
     * Ruta: POST /api/notificaciones/{id}/marcar-leida
     */
    @PostMapping("/{id}/marcar-leida")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        return notificacionRepository.findById(id).map(notif -> {
            notif.setLeida(true);
            notificacionRepository.save(notif);
            return ResponseEntity.ok().body("Notificación leída");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 2. LIMPIAR TODAS (El botón mágico que daba error 404)
    @PostMapping("/mark-all-read") 
    public ResponseEntity<?> marcarTodasComoLeidas(Authentication auth) {
        if (auth == null) return ResponseEntity.badRequest().build();
        
        usuarioRepository.findByUserName(auth.getName()).ifPresent(user -> {
            List<Notificacion> pendientes = notificacionRepository.findByUsuarioIdAndLeidaFalse(user.getId());
            pendientes.forEach(n -> n.setLeida(true));
            notificacionRepository.saveAll(pendientes);
        });
        
        return ResponseEntity.ok().body("Todas las notificaciones marcadas como leídas");
    }
}