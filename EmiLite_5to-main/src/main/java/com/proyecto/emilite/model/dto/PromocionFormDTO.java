package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Genera getters, setters, toString, equals, hashCode
public class PromocionFormDTO {

    @NotBlank(message = "El código de la promoción es obligatorio")
    private String codigo;

    private String descripcion;

    @DecimalMin(value = "0.00", message = "El descuento debe ser al menos 0.00")
    @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor al 100%")
    private BigDecimal descuentoPorcentaje;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDate fechaFin;

    @Min(value = 1, message = "El máximo de usos debe ser al menos 1")
    private Integer maxUsos = 1;

    // Indica si la promoción está activa
    private Boolean activa = true;


}