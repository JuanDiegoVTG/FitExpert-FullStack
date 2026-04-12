package com.proyecto.emilite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @GetMapping
    public String catalogoCliente() {
        return "cliente/catalogo";

    }


    



}
