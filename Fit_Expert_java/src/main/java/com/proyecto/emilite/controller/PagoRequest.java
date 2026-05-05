package com.proyecto.emilite.controller;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.Servicio;
import lombok.Data;

@Data
public class PagoRequest {
    private Usuario usuario;
    private Servicio servicio;
    private String metodoPago;
}
