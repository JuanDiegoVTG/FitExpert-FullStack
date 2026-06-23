package com.proyecto.emilite.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.emilite.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // 1. Busca todos los servicios donde 'activa' es true
    List<Servicio> findByActivaTrue();
    
    // 2. Busca servicios por nombre (útil para validaciones o búsquedas)
    List<Servicio> findByNombre(String nombre);

    // 3. Busca servicios por el estado booleano recibido (útil para filtros)
    List<Servicio> findByActiva(Boolean activa);
}