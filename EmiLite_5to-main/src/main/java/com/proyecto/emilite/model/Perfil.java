package com.proyecto.emilite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "perfil")
@Data // @Data ya incluye @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // Te permite crear objetos perfil más fácil en el servicio: Perfil.builder().edad(20)...
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    // Quitamos los @NotNull y @Positive de aquí para que el registro sea flexible.
    // Los validaremos más adelante cuando el usuario complete su configuración técnica.
    private Integer edad;
    private Double peso;
    private Double altura;
    private String objetivo;

    // Atributos Personales
    private String sexo;
    private String nivelActividad;

    // Medidas para cálculos
    private Double cuello;
    private Double cintura;
    private Double cadera;

    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Column(columnDefinition = "TEXT") // TEXT permite descripciones más largas
    private String observaciones;

    // Relación bidireccional con Usuario
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}