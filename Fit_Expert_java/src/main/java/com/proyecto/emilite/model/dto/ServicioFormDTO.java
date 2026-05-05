package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data; // Importa Lombok

import java.math.BigDecimal;

@Data // <-- Esta anotación de Lombok genera getter y setter para 'activo'
public class ServicioFormDTO {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre; // Requiere getNombre() y setNombre()

    private String descripcion; // Requiere getDescripcion() y setDescripcion()

    @NotNull(message = "La duración en minutos es obligatoria")
    @Positive(message = "La duración debe ser un número positivo")
    private Integer duracionMinutos; // Requiere getDuracionMinutos() y setDuracionMinutos()

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal precio; // Requiere getPrecio() y setPrecio()

    private Boolean activo = true;
}