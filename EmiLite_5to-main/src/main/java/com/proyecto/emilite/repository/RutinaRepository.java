package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    // Método para encontrar rutinas por ID del cliente
    List<Rutina> findByClienteId(Long clienteId);

    // Método para encontrar rutinas por estado activo
    List<Rutina> findByActivo(Boolean activo);

    // Método para encontrar rutinas por cliente y estado
    List<Rutina> findByClienteIdAndActivo(Long clienteId, Boolean activo);
}