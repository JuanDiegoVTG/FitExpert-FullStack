package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Método para encontrar pagos por ID de usuario (clave para la vista del cliente)
    List<Pago> findByUsuarioId(Long usuarioId);
}