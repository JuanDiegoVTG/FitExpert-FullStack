package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    // Spring Boot crea la consulta SQL automáticamente basándose en este nombre:
    // Trae todas las notificaciones de un usuario específico donde "leida" sea false.
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long usuarioId);

    // Para el historial completo del entrenador
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    // Para contar rápidamente cuántas no ha leído (ideal para poner el numerito rojo en el ícono)
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}