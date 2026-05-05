package com.proyecto.emilite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.emilite.model.Pago;


public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Método para encontrar pagos por ID de usuario (clave para la vista del cliente)
    List<Pago> findByUsuarioId(Long usuarioId);

    Optional<Pago> findByReferenciaPago(String referenciaPago);
}