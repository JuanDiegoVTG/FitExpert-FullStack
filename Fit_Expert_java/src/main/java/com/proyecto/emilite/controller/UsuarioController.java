package com.proyecto.emilite.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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

@SuppressWarnings("null")
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

        // 2. PROCESO DE REGISTRO, IA Y GUARDADO DE PDF
        Double scoreObtenido = 0.0; 
        String nombreArchivoFinal = null;

        try {
            if (usuarioForm.getRolId() == 2) { 
                if (archivoCv != null && !archivoCv.isEmpty()) {
                    String rutaCarpeta = "/home/juand/uploads/cvs/"; 
                    File directorio = new File(rutaCarpeta);
                    if (!directorio.exists()) directorio.mkdirs();

                    nombreArchivoFinal = UUID.randomUUID().toString() + "_" + archivoCv.getOriginalFilename();
                    Path rutaCompleta = Paths.get(rutaCarpeta + nombreArchivoFinal);
                    Files.write(rutaCompleta, archivoCv.getBytes());
                    
                    scoreObtenido = pythonService.validarCvConPython(archivoCv, usuarioForm.getUserName());
                    System.out.println("🐍 CV guardado y analizado. Score: " + scoreObtenido);
                }
            }

            // 3. REGISTRO EN BD
            usuarioService.registrarConCv(usuarioForm, nombreArchivoFinal);
            try {
                if (usuarioForm.getRolId() == 2) {
                    emailService.enviarNotificacionRegistro(
                        usuarioForm.getEmail(), 
                        usuarioForm.getNombres(), 
                        scoreObtenido
                    );
                }
            } catch (Exception mailError) {
                System.err.println("⚠️ No se pudo enviar el correo: " + mailError.getMessage());
            }
            
            return "redirect:/login?exito=true";

        } catch (Exception e) {
            model.addAttribute("error", "Error en el proceso: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "registro_publico";
        }
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuarioForm) {
        Usuario entrenador = usuarioService.findById(usuarioForm.getId());

        entrenador.setDescripcion(usuarioForm.getDescripcion());
        entrenador.setNombres(usuarioForm.getNombres());
        entrenador.setApellidos(usuarioForm.getApellidos());
        entrenador.setEmail(usuarioForm.getEmail());

        usuarioService.save(entrenador);
        return "redirect:/catalogo?exito=true";
    }

    // 🛡️ MÉTODO CORREGIDO CON ID LONG PARA TU REPOSITORIO NATIVO
    @PostMapping("/guardar-hoja-vida")
    public String guardarHojaVidaEntrenador(@RequestParam("usuarioId") Long usuarioId, 
                                            @RequestParam("archivoHojaVida") MultipartFile archivo) {
        
        // 1. Ahora que el ID es Long, el findById va a cuadrar perfectamente sin errores
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
        
        // 2. Enviar el PDF a MongoDB a través del microservicio PHP
        String mongoId = pdfService.subirPdfAMongo(archivo);
        
        // 3. Si la subida fue exitosa, le asociamos el ID NoSQL a su perfil de usuario
        if (mongoId != null) {
            usuario.setHojaVidaMongoId(mongoId);
            usuarioRepository.save(usuario);
        }
        
        // Redirecciona al panel de tus usuarios
        return "redirect:/admin/usuarios"; 
    }
}