package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern; 
import lombok.Data;

@Data // Genera getters, setters, toString, equals, hashCode
public class ClientePerfilDTO {

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe ser un número válido (7 a 15 dígitos)")
    private String telefono;

    private String direccion;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
}