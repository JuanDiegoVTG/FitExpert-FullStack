package com.proyecto.emilite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.ui.Model;
import java.util.List;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.service.UsuarioService;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String catalogoCliente(Model model) {

        List<Usuario> entrenadores = usuarioService.findByRolNombre("ENTRENADOR");

        model.addAttribute("entrenadores", entrenadores);

        return "cliente/catalogo";
    }
}


    




