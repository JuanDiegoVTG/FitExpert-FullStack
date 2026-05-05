package com.proyecto.emilite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.emilite.model.Promocion;


public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    // Método para buscar una promoción por su código (único)
    Optional<Promocion> findByCodigo(String codigo);

    // Comprueba si existe una promoción con el código dado
    boolean existsByCodigo(String codigo);

    // Método para encontrar promociones activas 
    List<Promocion> findByActivaTrue();

    
}