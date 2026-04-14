package com.proyecto.emilite.model;

import java.time.LocalDate;


import jakarta.persistence.Id; 

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn; 
import jakarta.persistence.Table;      
import lombok.Data;

@Data
@Entity
@Table(name = "contratos") 
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id") // Define el nombre de la columna en la BD
    private Usuario cliente; 

    @ManyToOne
    @JoinColumn(name = "entrenador_id")
    private Usuario entrenador; 

    private boolean activo = true;
    private LocalDate fechaInicio;
}