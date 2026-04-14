package com.proyecto.emilite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.emilite.model.Contrato;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    // Verifica si existe un contrato activo entre el cliente y el entrenador
    boolean existsByClienteIdAndActivoTrue(Long clienteId);

    // Para buscar el contrato específico y poder desactivarlo o editarlo
    Optional<Contrato> findByClienteIdAndActivoTrue(Long clienteId);
    
    // Para saber qué entrenador tiene un cliente específico
    Optional<Contrato> findByClienteId(Long clienteId);
}