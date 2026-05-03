package com.proyecto.emilite.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "username", unique = true, nullable = false)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String userName; 


    @Column(name = "password", nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Column(name = "email", unique = true)
    @Email(message = "El email debe tener un formato válido")
    private String email; 

    @Column(name = "nombres", nullable = false)
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos; 

    @Column(name = "telefono")
    private String telefono; 

    @Column(name = "direccion")
    private String direccion; 

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento; 

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ToString.Exclude
    @OneToOne(mappedBy = "usuario",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Perfil perfil;
    public Perfil getPerfil() {
    return perfil;
    }

    // Relación con la tabla rol
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;


    @Column(name = "activo", nullable = false)
    private Boolean activo = true; 

    @JsonIgnore
    @OneToMany(mappedBy = "entrenador", cascade = CascadeType.ALL)
    private List<Usuario> alumnos = new ArrayList<>();

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Usuario entrenador;

    private String edad;

    
    @Column(name = "validado")
    private boolean validado = false; // El campo que usa la IA y el Admin
    private boolean enabled = true;   // Para que Spring Security sepa que la cuenta está activa

    @Column(name = "ruta_hoja_vida")
    private String rutaHojaVida;

    private Double cvScore;

    private Boolean esPremuim;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles = new ArrayList<>();
    
    public void setEntrenador(Usuario entrenador) {
        // Si el usuario ya tenía un entrenador, lo sacamos de la lista del anterior
        if (this.entrenador != null) {
            this.entrenador.getAlumnos().remove(this);
        }
        this.entrenador = entrenador;
        // Agregamos este usuario a la lista del nuevo entrenador
        if (entrenador != null) {
            entrenador.getAlumnos().add(this);
        }
    }


}