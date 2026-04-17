package com.proyecto.emilite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern; 
import lombok.Data;

@Data 
public class ClientePerfilDTO {

    private Long id;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[\\+0-9\\s\\-]{7,20}$", message = "El teléfono debe tener un formato válido")
    private String telefono;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    // Estos los dejamos sin @NotBlank porque usas 'nombreCompleto' en el form
    private String nombres;
    private String apellidos;
    
    private String direccion;
    private Integer edad;
    private String sexo;
    private Double peso;
    private Double altura;
    private String nivelActividad;
    private Double cuello;
    private Double cintura;
    private Double cadera;
    private String objetivo;
}