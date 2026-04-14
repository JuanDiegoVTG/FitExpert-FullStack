package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    // Spring Boot crea la consulta SQL automáticamente basándose en este nombre:
    // Trae todas las notificaciones de un usuario específico donde "leida" sea false.
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
}