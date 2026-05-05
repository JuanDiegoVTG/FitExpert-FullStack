package com.proyecto.emilite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso")
@Data // Si usas Lombok, si no, genera los Getters y Setters manualmente
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProgreso;

    // Relación con el usuario: Muchos registros de progreso para un solo usuario
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private Double cintura;
    private Double cuello;
    
    private Double cadera;

    private Double peso;
    private Double grasa;
    private Double imc;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Constructor que se ejecuta antes de guardar para poner la fecha actual
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}