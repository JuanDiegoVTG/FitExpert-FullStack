package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Progreso;
import com.proyecto.emilite.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgresoRepository extends JpaRepository<Progreso, Long> {
    // Buscar el historial de un usuario ordenado por fecha (el más reciente primero)
    List<Progreso> findByUsuarioOrderByFechaRegistroAsc(Usuario usuario);
}