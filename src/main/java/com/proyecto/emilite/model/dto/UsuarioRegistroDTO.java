package com.proyecto.emilite.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data; 
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Data 
public class UsuarioRegistroDTO {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 4, max = 20, message = "El username debe tener entre 4 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "El username no puede tener espacios ni caracteres especiales")
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
    private String descripcion;

    @NotNull(message = "El rol es obligatorio")
    private Long rolId; 
    
    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}
