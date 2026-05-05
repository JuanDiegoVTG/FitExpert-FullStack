package com.proyecto.emilite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id") 
    private Servicio servicio; 

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser un número positivo")
    private BigDecimal monto; 

    @Column(name = "metodo_pago", nullable = false, length = 50)
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago; 

    @Column(name = "estado", nullable = false, length = 20)
    @NotBlank(message = "El estado del pago es obligatorio")
    private String estado = "PENDIENTE"; 

    @Column(name = "referencia_pago", unique = true, length = 100)
    private String referenciaPago; 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id") 
    private Promocion promocion; 

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now(); 
}