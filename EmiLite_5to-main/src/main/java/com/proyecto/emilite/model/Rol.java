package com.proyecto.emilite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "rol")
@Data 
@AllArgsConstructor
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre; 

    @Column(name = "descripcion", length = 255)
    private String descripcion; 

    @Column(name = "activo", nullable = false)
    private Boolean activo = true; 
}