package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.service.RolService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService; 
        //Mostrar el formulario de registro para cualquier usuario no logueado
    @GetMapping("/usuarios/registro-publico") // <-- Ruta específica para registro público
    public String mostrarFormularioRegistroPublico(Model model) {
        // Cargar solo el rol CLIENTE para el registro público
        List<Rol> rolesCliente = rolService.findByNombre("CLIENTE");
        if (rolesCliente.isEmpty()) {
            model.addAttribute("error", "No se puede registrar en este momento. Contacte al administrador.");
            return "error"; // Vista genérica de error
        }

        // Crear el DTO de formulario y fijar el rolId por defecto (primer rol CLIENTE)
        UsuarioRegistroDTO usuarioForm = new UsuarioRegistroDTO();
        usuarioForm.setRolId(rolesCliente.get(0).getId());

        model.addAttribute("usuarioForm", usuarioForm);
        model.addAttribute("roles", rolesCliente); // Pasa solo el rol CLIENTE
        return "registro_publico";
    }

    
    // Procesar el formulario de registro público
    @PostMapping("/usuarios/crear-publico") // 
    public String crearUsuarioPublico(@Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
                                      BindingResult result,
                                      Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            List<Rol> rolesCliente = rolService.findByNombre("CLIENTE");
            model.addAttribute("roles", rolesCliente);
            return "registro_publico";
        }

        // Verificar si el nombre de usuario ya existe
        if (usuarioService.existsByUserName(usuarioForm.getUserName())) {
            result.rejectValue("userName", "error.usuarioForm", "El nombre de usuario ya está en uso.");
            List<Rol> rolesCliente = rolService.findByNombre("CLIENTE");
            model.addAttribute("roles", rolesCliente);
            return "registro_publico";
        }

        // Verificar si el email ya existe (opcional, si tienes email único)
        if (usuarioForm.getEmail() != null && !usuarioForm.getEmail().isEmpty() &&
            usuarioService.existsByEmail(usuarioForm.getEmail())) {
            result.rejectValue("email", "error.usuarioForm", "El email ya está en uso.");
            List<Rol> rolesCliente = rolService.findByNombre("CLIENTE");
            model.addAttribute("roles", rolesCliente);
            return "registro_publico";
        }
    

        try {
            // Asignar el rol CLIENTE por defecto
           
                // Obtener el rol CLIENTE usando el helper del servicio (devuelve Optional)
                Rol rolCliente = rolService.findOneByNombre("CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

                // Asignar el ID del rol al DTO antes de pasarlo al servicio
                usuarioForm.setRolId(rolCliente.getId());

            // Llamar al servicio para crear el usuario
            usuarioService.crearUsuarioDesdeDTO(usuarioForm);

            // Redirigir a la página de login con un mensaje de éxito
            return "redirect:/login?registrado=success";

        } catch (Exception e) {
            // Manejo de errores inesperados
            model.addAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            List<Rol> rolesCliente = rolService.findByNombre("CLIENTE");
            model.addAttribute("roles", rolesCliente);
            return "registro_publico";
        }
    }

}
