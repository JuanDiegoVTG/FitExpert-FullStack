package com.proyecto.emilite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- IMPORTANTE: No olvides este import

import com.proyecto.emilite.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // CONSULTA PARA SUMAR LAS VENTAS TOTALES
    @Query("SELECT SUM(p.monto) FROM Pago p")
    Double sumarTotalVentas();

    // Método para encontrar pagos por ID de usuario (clave para la vista del cliente)
    List<Pago> findByUsuarioId(Long usuarioId);

    Optional<Pago> findByReferenciaPago(String referenciaPago);
}