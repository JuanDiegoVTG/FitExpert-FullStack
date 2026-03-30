package com.proyecto.emilite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*; // Importa Lombok

import java.time.LocalDateTime;

@Entity
@Table(name = "rutina")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre de la rutina es obligatorio")
    private String nombre; 

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion; 
    @Column(name = "nivel_dificultad", length = 50)
    private String nivelDificultad; 

    @Column(name = "tipo", length = 50)
    private String tipo; 

    @Column(name = "duracion_semanas")
    @Positive(message = "La duración debe ser un número positivo")
    private Integer duracionSemanas; 

    // Relación con la tabla usuario 
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "cliente_id", nullable = false) 
    @NotNull(message = "El cliente es obligatorio")
    private Usuario cliente; 

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now(); 

    @Column(name = "activo", nullable = false)
    private Boolean activo = true; 
}