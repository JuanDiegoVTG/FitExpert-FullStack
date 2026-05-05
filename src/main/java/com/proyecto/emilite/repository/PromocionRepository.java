package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    // Método para buscar una promoción por su código (único)
    Optional<Promocion> findByCodigo(String codigo);

    // Comprueba si existe una promoción con el código dado
    boolean existsByCodigo(String codigo);

    // Método para encontrar promociones activas 
    List<Promocion> findByActivaTrue();

    
}