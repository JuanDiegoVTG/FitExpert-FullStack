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

    // 1. Mostrar el formulario con los roles disponibles (Atleta y Entrenador)
    @GetMapping("/usuarios/registro-publico")
    public String mostrarFormularioRegistroPublico(Model model) {
        List<Rol> rolesDisponibles = rolService.findAll(); 
        UsuarioRegistroDTO usuarioForm = new UsuarioRegistroDTO();
        
        model.addAttribute("usuarioForm", usuarioForm);
        model.addAttribute("roles", rolesDisponibles); 
        return "registro_publico"; 
    }

    // 2. Procesar el formulario de registro
    @PostMapping("/usuarios/crear-publico")
    public String crearUsuarioPublico(
            @Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
            BindingResult result,
            @RequestParam(value = "archivoCv", required = false) MultipartFile archivoCv, 
            Model model) {

        // A. VALIDACIONES INICIALES
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

        // B. INTENTO DE REGISTRO E INTEGRACIÓN
        // 1. Declaramos la variable AQUÍ arriba para que todo el bloque 'try' la vea
        Double scoreObtenido = 0.0; 

        try {
            // 2. CAPTURAMOS el resultado que viene de Python
            if (usuarioForm.getRolId() == 2 && archivoCv != null && !archivoCv.isEmpty()) {
                scoreObtenido = pythonService.validarCvConPython(archivoCv, usuarioForm.getUserName());
            }

            // 3. Guardamos el usuario
            usuarioService.registrar(usuarioForm);

            // 4. DISPARAMOS EL CORREO (Dentro del try, para que solo se envíe si el registro fue exitoso)
            if (usuarioForm.getRolId() == 2) {
                emailService.enviarNotificacionRegistro(usuarioForm.getEmail(), usuarioForm.getNombres(), scoreObtenido);
            }
            
            return "redirect:/login?exito=true";

        } catch (Exception e) {
            model.addAttribute("error", "Error en el proceso: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }
    }

}