package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Método para encontrar roles por nombre
    List<Rol> findByNombre(String nombre);
}