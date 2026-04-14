package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
   // servicios activos:
    List<Servicio> findByActivoTrue();
    
    List<Servicio> findByNombre(String nombre);

     // Método para encontrar servicios por estado activo/inactivo
   List<Servicio> findByActivo(Boolean activo);
}