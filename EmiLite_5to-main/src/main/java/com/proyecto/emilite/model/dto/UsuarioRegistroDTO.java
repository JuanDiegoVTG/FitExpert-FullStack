package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data; 

import java.time.LocalDate;

@Data 
public class UsuarioRegistroDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String userName; 

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password; 

    @Email(message = "El email debe tener un formato válido")
    private String email; 

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres; 

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos; 

    private String telefono; 

    private String direccion; 

    private LocalDate fechaNacimiento; 

    @NotNull(message = "El rol es obligatorio")
    private Long rolId; 
}