package com.proyecto.emilite.controller;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.emilite.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.EmailService;
import com.proyecto.emilite.service.MicroservicioPdfService;
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

    @Autowired
    private MicroservicioPdfService pdfService;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @GetMapping("/usuarios/registro-publico")
    public String mostrarFormularioRegistroPublico(Model model) {
        List<Rol> rolesDisponibles = rolService.findAll(); 
        model.addAttribute("usuarioForm", new UsuarioRegistroDTO());
        model.addAttribute("roles", rolesDisponibles); 
        return "registro_publico"; 
    }

   @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(LocalDate.parse(text, DateTimeFormatter.ISO_DATE));
            }
        });
    }

    @PostMapping("/usuarios/crear-publico")
    public String crearUsuarioPublico(
            @Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
            BindingResult result,
            @RequestParam(value = "archivoCv", required = false) MultipartFile archivoCv, 
            Model model) {

        // 1. Validaciones de fecha de nacimiento (Solo si Spring logró convertirla)
        if (usuarioForm.getFechaNacimiento() != null) {
            int edadCalculada = Period.between(usuarioForm.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edadCalculada < 16) {
                result.rejectValue("fechaNacimiento", "error.usuarioForm", "Debes tener al menos 16 años.");
            }
        } else {
            // Si llega nula, es probable que la conversión falló, pero BindingResult ya tiene el error
            if(!result.hasFieldErrors("fechaNacimiento")) {
                result.rejectValue("fechaNacimiento", "error.usuarioForm", "Selecciona tu fecha de nacimiento.");
            }
        }

        // Si hay errores de validación (incluyendo formato de fecha), volvemos al form
        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }

        // 2. Variables de control
        String mongoId = null;
        Double scoreObtenido = 0.0;

        try {
            // A. Procesamiento de archivo solo si existe
            if (archivoCv != null && !archivoCv.isEmpty()) {
                mongoId = pdfService.subirPdfAMongo(archivoCv);
                scoreObtenido = pythonService.validarCvConPython(archivoCv, usuarioForm.getUserName());
                System.out.println("DEBUG: ID recibido del microservicio PHP: " + mongoId);
            } else {
                System.out.println("DEBUG: El archivo cv es nulo o vacío.");
            }

            // B. Registro del usuario (El Service manejará la asignación de rol internamente)
            usuarioService.registrarConCv(usuarioForm, mongoId);
            
            // C. Notificación (Independiente del éxito del flujo principal)
            try {
                emailService.enviarNotificacionRegistro(usuarioForm.getEmail(), usuarioForm.getNombres(), scoreObtenido);
            } catch (Exception mailError) {
                System.err.println("Aviso: Correo no enviado, pero registro exitoso: " + mailError.getMessage());
            }
            
            return "redirect:/login?exito=true";

        } catch (Exception e) {
            // D. Gestión de errores críticos (BD, Conexión Mongo, Python)
            model.addAttribute("error", "Error inesperado al procesar el registro: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }
    }
    

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuarioForm) {
        Usuario entrenador = usuarioService.findById(usuarioForm.getId());

        // Verificación de seguridad para evitar NullPointerException si el ID no existe
        if (entrenador != null) {
            entrenador.setDescripcion(usuarioForm.getDescripcion());
            entrenador.setNombres(usuarioForm.getNombres());
            entrenador.setApellidos(usuarioForm.getApellidos());
            entrenador.setEmail(usuarioForm.getEmail());

            usuarioService.save(entrenador);
        }
        
        return "redirect:/catalogo?exito=true";
    }

    // 🛡️ MÉTODO SEGURO CON ID LONG PARA EL REPOSITORIO NATIVO
    @PostMapping("/guardar-hoja-vida")
    public String guardarHojaVidaEntrenador(@RequestParam("usuarioId") Long usuarioId, 
                                            @RequestParam("archivoHojaVida") MultipartFile archivo) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
        
        // Enviar el PDF a MongoDB a través del microservicio PHP
        String mongoId = pdfService.subirPdfAMongo(archivo);
        
        // Asocia el ID NoSQL si la subida fue exitosa
        if (mongoId != null && !mongoId.isEmpty()) {
            usuario.setHojaVidaMongoId(mongoId);
            usuarioRepository.save(usuario);
        }
        
        return "redirect:/admin/usuarios"; 
    }
}