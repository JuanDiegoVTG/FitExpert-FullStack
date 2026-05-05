package com.proyecto.emilite.controller;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ReporteController {

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar el formulario de reportes
    @GetMapping("/reportes")
    public String mostrarFormulario() {
        return "reportes";
    }

    // Procesar el formulario y mostrar el reporte
    @PostMapping("/reportes")
    public String generarReporte(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String activo,
            Model model) {

        // Convertir el parámetro "activo" a Boolean
        Boolean activoBoolean = null;
        if (activo != null && !activo.isEmpty()) {
            activoBoolean = Boolean.parseBoolean(activo);
        }

        // Obtener los usuarios filtrados
        List<Usuario> usuarios = usuarioService.findByFilters(rol, activoBoolean);

        // Agregar los usuarios al modelo para que la vista pueda mostrarlos
        model.addAttribute("usuarios", usuarios);

        // Devolver la vista que muestra el reporte
        return "reporte_resultado";
    }
}