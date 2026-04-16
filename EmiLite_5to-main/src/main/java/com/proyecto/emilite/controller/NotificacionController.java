package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.emilite.model.Notificacion;
import com.proyecto.emilite.repository.NotificacionRepository;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    // Endpoint para consultar las notificaciones no leídas
    @GetMapping("/entrenador/{entrenadorId}/pendientes")
    public ResponseEntity<List<Notificacion>> obtenerPendientes(@PathVariable Long entrenadorId) {
        List<Notificacion> pendientes = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(entrenadorId);
        return ResponseEntity.ok(pendientes);
    }
    
    // Endpoint para apagar la campanita (marcar como leída)
    @PostMapping("/{id}/marcar-leida")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        Notificacion notificacion = notificacionRepository.findById(id).orElse(null);
        if (notificacion != null) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
            return ResponseEntity.ok().body("Notificación marcada como leída");
        }
        return ResponseEntity.notFound().build();
    }
}