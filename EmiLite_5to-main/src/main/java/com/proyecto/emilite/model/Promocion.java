package com.proyecto.emilite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promocion")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "codigo", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El código de la promoción es obligatorio")
    private String codigo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "descuento_porcentaje", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "El descuento debe ser al menos 0.00")
    @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor al 100%")
    private BigDecimal descuentoPorcentaje;

    @Column(name = "fecha_inicio", nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDate fechaFin;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Column(name = "max_usos", nullable = false)
    @Min(value = 1, message = "El máximo de usos debe ser al menos 1")
    private Integer maxUsos = 1;

    @Column(name = "usos_actuales", nullable = false)
    @Min(value = 0, message = "Los usos actuales no pueden ser negativos")
    private Integer usosActuales = 0;
}