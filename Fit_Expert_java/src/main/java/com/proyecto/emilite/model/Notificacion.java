package com.proyecto.emilite.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El entrenador que recibe la notificación
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; 

    // El cliente que contrata/paga
    @ManyToOne
    @JoinColumn(name = "remitente_id")
    private Usuario remitente; 

    private String mensaje;
    
    private Boolean leida = false;
    

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

}