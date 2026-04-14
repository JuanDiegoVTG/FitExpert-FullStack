package com.proyecto.emilite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.service.ServicioService;

import jakarta.validation.Valid;

@Controller
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    // Vista para el CLIENTE
    @GetMapping("/servicios")
    public String listarServiciosCliente(Model model) {
        model.addAttribute("servicios", servicioService.findByActivo(true));
        return "cliente/ver_servicios"; 
    }

    // Vista para el ADMIN (Lista)
    @GetMapping("/admin/servicios")
    public String listarServiciosAdmin(Model model) {
        model.addAttribute("servicios", servicioService.findAll());
        return "admin/servicios/lista_servicios";
    }

    // Formulario Nuevo (ADMIN)
    @GetMapping("/admin/servicios/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("servicio", new Servicio()); 
        return "admin/servicios/form_servicio"; 
    }

    // Guardar (ADMIN)
    @PostMapping("/admin/servicios/guardar")
    public String guardarServicio(@Valid @ModelAttribute("servicio") Servicio servicio, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/servicios/form_servicio";
        }
        if (servicio.getId() == null) servicio.setActivo(true);
        servicioService.save(servicio);
        return "redirect:/admin/servicios";
    }

    // Editar (ADMIN)
    @GetMapping("/admin/servicios/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("servicio", servicioService.findById(id));
        model.addAttribute("servicioId", id);
        return "admin/servicios/form_servicio";
    }

    // Eliminar (ADMIN)
    @PostMapping("/admin/servicios/{id}/eliminar")
    public String eliminarServicio(@PathVariable Long id) {
        Servicio servicio = servicioService.findById(id);
        
        // En lugar de borrarlo, lo desactivamos
        servicio.setActivo(false); 
        servicioService.save(servicio);
        
        return "redirect:/admin/servicios";
    }
}