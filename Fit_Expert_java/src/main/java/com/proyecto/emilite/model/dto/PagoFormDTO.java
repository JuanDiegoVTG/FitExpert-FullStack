package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data // Genera getters, setters, toString, equals, hashCode
public class PagoFormDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId; 

    private Long servicioId; 

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser un número positivo")
    private BigDecimal monto; 
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago; 

    @NotBlank(message = "El estado del pago es obligatorio")
    private String estado = "PENDIENTE"; 

    private String referenciaPago; 

    private Long promocionId; 
}