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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import com.proyecto.emilite.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.model.Rol;
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

        // 1. VALIDACIONES DE FORMULARIO (Edad y existencia)
        if (usuarioForm.getFechaNacimiento() != null) {
            LocalDate hoy = LocalDate.now();
            // Calculamos el periodo entre la fecha de nacimiento y hoy
            int edadCalculada = Period.between(usuarioForm.getFechaNacimiento(), hoy).getYears();

            if (edadCalculada < 16) {
                result.rejectValue("fechaNacimiento", "error.usuarioForm", "Debes tener al menos 16 años para registrarte.");
            }
        } else {
            result.rejectValue("fechaNacimiento", "error.usuarioForm", "Por favor, selecciona tu fecha de nacimiento.");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }

        // ... (tus validaciones de nombre de usuario y email siguen igual) ...

        // 2. PROCESO DE REGISTRO, IA Y GUARDADO DE PDF
        Double scoreObtenido = 0.0; 
        String nombreArchivoFinal = null;

        try {
            // REGLA: 2 = Entrenador (Análisis de CV y Guardado Físico)
            if (usuarioForm.getRolId() == 2) { 
                if (archivoCv != null && !archivoCv.isEmpty()) {
                    // --- LÓGICA DE GUARDADO FÍSICO ---
                    String rutaCarpeta = "/home/juand/uploads/cvs/"; // Ruta en tu WSL
                    File directorio = new File(rutaCarpeta);
                    if (!directorio.exists()) directorio.mkdirs();

                    // Nombre único: uuid_nombre.pdf
                    nombreArchivoFinal = UUID.randomUUID().toString() + "_" + archivoCv.getOriginalFilename();
                    Path rutaCompleta = Paths.get(rutaCarpeta + nombreArchivoFinal);
                    Files.write(rutaCompleta, archivoCv.getBytes());
                    
                    // --- LÓGICA DE PYTHON ---
                    scoreObtenido = pythonService.validarCvConPython(archivoCv, usuarioForm.getUserName());
                    System.out.println("🐍 CV guardado y analizado. Score: " + scoreObtenido);
                }
            }

            // 3. REGISTRO EN BD
            // Pasamos el nombre del archivo al servicio para que lo guarde en el campo 'rutaHojaVida'
            usuarioService.registrarConCv(usuarioForm, nombreArchivoFinal);
            try{
                // 4. NOTIFICACIÓN POR CORREO
                if (usuarioForm.getRolId() == 2) {
                    emailService.enviarNotificacionRegistro(
                        usuarioForm.getEmail(), 
                        usuarioForm.getNombres(), 
                        scoreObtenido
                    );
                }
                } catch (Exception mailError) {
                // Solo imprimimos el error en consola, pero dejamos que el usuario siga
                System.err.println("⚠️ No se pudo enviar el correo: " + mailError.getMessage());
            }
            
            return "redirect:/login?exito=true";

        } catch (Exception e) {
            model.addAttribute("error", "Error en el proceso: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }
    }
}