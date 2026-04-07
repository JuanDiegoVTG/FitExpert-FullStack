package com.proyecto.emilite.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.service.EmailService;
import com.proyecto.emilite.service.PythonService;
import com.proyecto.emilite.service.RolService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService; 

    @Autowired
    private PythonService pythonService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/usuarios/registro-publico")
    public String mostrarFormularioRegistroPublico(Model model) {
        List<Rol> rolesDisponibles = rolService.findAll(); 
        UsuarioRegistroDTO usuarioForm = new UsuarioRegistroDTO();
        
        model.addAttribute("usuarioForm", usuarioForm);
        model.addAttribute("roles", rolesDisponibles); 
        return "registro_publico"; 
    }

    @PostMapping("/usuarios/crear-publico")
    public String crearUsuarioPublico(
            @Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
            BindingResult result,
            @RequestParam(value = "archivoCv", required = false) MultipartFile archivoCv, 
            Model model) {

        // 1. VALIDACIONES DE FORMULARIO
        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }

        if (usuarioService.existsByUserName(usuarioForm.getUserName())) {
            result.rejectValue("userName", "error.usuarioForm", "Este nombre de usuario ya está en uso.");
        }
        
        if (usuarioForm.getEmail() != null && !usuarioForm.getEmail().isEmpty() &&
            usuarioService.existsByEmail(usuarioForm.getEmail())) {
            result.rejectValue("email", "error.usuarioForm", "Este email ya está registrado.");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }

        // 2. PROCESO DE REGISTRO E IA
        Double scoreObtenido = 0.0; 

        try {
            //  REGLA: 2 = Entrenador (Análisis de CV con Python)
            if (usuarioForm.getRolId() == 2) { 
                if (archivoCv != null && !archivoCv.isEmpty()) {
                    scoreObtenido = pythonService.validarCvConPython(archivoCv, usuarioForm.getUserName());
                    System.out.println("🐍 Python analizó el CV. Score: " + scoreObtenido);
                }
            }

            // Guardamos el usuario (el Service ya sabe que si es 2, validado = false)
            usuarioService.registrar(usuarioForm);

            // REGLA: 2 = Entrenador (Disparar el correo de postulación)
            if (usuarioForm.getRolId() == 2) {
                emailService.enviarNotificacionRegistro(
                    usuarioForm.getEmail(), 
                    usuarioForm.getNombres(), 
                    scoreObtenido
                );
                System.out.println("📧 Correo de postulación enviado a: " + usuarioForm.getEmail());
            }
            
            return "redirect:/login?exito=true";

        } catch (Exception e) {
            System.err.println("❌ Error en el proceso: " + e.getMessage());
            model.addAttribute("error", "Error en el proceso: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }
    }
}