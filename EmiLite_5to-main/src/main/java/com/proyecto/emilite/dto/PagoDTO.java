package com.proyecto.emilite.dto;

import lombok.Data;

@Data
public class PagoDTO {
    private Long usuarioId;      // Faltaba este campo
    private String referencia;
    private double monto;
    private String descripcion;
    private String metodoPago;   // NEQUI, TARJETA, PSE
    private String estado;       // PENDIENTE, COMPLETADO, FALLIDO
}