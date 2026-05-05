package com.proyecto.emilite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.emilite.model.Servicio;


public interface ServicioRepository extends JpaRepository<Servicio, Long> {
   // servicios activos:
    List<Servicio> findByActivoTrue();
    
    List<Servicio> findByNombre(String nombre);

     // Método para encontrar servicios por estado activo/inactivo
   List<Servicio> findByActivo(Boolean activo);
}