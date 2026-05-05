package com.proyecto.emilite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.emilite.model.Rol;


public interface RolRepository extends JpaRepository<Rol, Long> {
    // Método para encontrar roles por nombre
    List<Rol> findByNombre(String nombre);
}