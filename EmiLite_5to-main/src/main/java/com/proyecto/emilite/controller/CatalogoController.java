package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.service.UsuarioService;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra el catálogo de entrenadores.
     * Soporta filtrado por palabra clave (nombre/especialidad) y calificación.
     */
    @GetMapping
    public String catalogoCliente(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "calificacion", required = false) Integer calificacion,
            Model model) {

        List<Usuario> entrenadores;

        // 1. Lógica de Filtrado Dinámico
        if (keyword != null && !keyword.isEmpty()) {
            // Si el usuario usa el buscador de Kevin
            entrenadores = usuarioService.buscarPorNombreOEspecialidad(keyword);
        } else if (calificacion != null) {
            // Si el usuario filtra por estrellas
            entrenadores = usuarioService.buscarPorCalificacion(calificacion);
        } else {
            // Por defecto, carga todos los que tengan el rol de ENTRENADOR
            entrenadores = usuarioService.findByRolNombre("ENTRENADOR");
        }

        // 2. Pasamos la lista al HTML (Debe llamarse "entrenadores" para el th:each)
        model.addAttribute("entrenadores", entrenadores);
        
        // 3. Devolvemos los filtros para que no se borren de los inputs al recargar
        model.addAttribute("keyword", keyword);
        model.addAttribute("calificacion", calificacion);

        return "cliente/catalogo";
    }
}