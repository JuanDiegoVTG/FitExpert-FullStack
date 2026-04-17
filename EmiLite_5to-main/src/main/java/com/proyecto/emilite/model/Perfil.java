package com.proyecto.emilite.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotNull(message = "La edad es obligatoria")
    @Positive(message = "La edad debe ser un número positivo")
    private Integer edad;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser un número positivo")
    private Double peso;

    @NotNull(message = "La estatura es obligatoria")
    @Positive(message = "La estatura debe ser un número positivo")
    private Double altura;

    @NotNull(message = "El objetivo es obligatorio")
    private String objetivo;

    //Atrivutos Personales
    private String sexo;
    private String nivelActividad;

    // Medidas para cálculos de composición corporal
    private Double cuello;
    private Double cintura;
    private Double cadera;


    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    private String email;

    private String observaciones;

    // Relación bidireccional con Usuario
    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "entrenador_id")
    private Usuario entrenador;
}