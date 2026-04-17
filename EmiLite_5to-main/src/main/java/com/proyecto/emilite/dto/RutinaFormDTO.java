package com.proyecto.emilite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data; // Importa Lombok

@Data 
public class RutinaFormDTO {

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    private String nombre;

    private String descripcion;

    private String nivelDificultad;

    private String tipo;

    @Positive(message = "La duración debe ser un número positivo")
    private Integer duracionSemanas;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    private Boolean activa = true;
}