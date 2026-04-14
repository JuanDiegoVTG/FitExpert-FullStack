package com.proyecto.emilite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.emilite.service.CatalogoService;


@Controller
@RequestMapping("/catalogo")
public class CatalogoController {
    
    @Autowired
    private CatalogoService catalogoService;

    

    @GetMapping
    public String catalogoCliente(Model model) {

        model.addAttribute("entrenadores",
                catalogoService.obtenerEntrenadoresActivos());

        return "cliente/catalogo";
    }


    



}
