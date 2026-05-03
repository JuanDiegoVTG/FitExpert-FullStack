package com.proyecto.emilite.model; // Ajusta a tu paquete

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "mensaje")
@Data // Esto te ahorra escribir getters y setters (usa Lombok)
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario sender; // Quién envía

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario receiver; // Quién recibe

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // El mensaje en sí

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    // Esto se ejecuta antes de guardar para poner la hora actual automáticamente
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}