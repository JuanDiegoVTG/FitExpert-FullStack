package com.proyecto.emilite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.emilite.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.MicroservicioPdfService;
import com.proyecto.emilite.service.RolService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/usuarios")
@SuppressWarnings("null")
public class AdminUsuarioController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MicroservicioPdfService pdfService;

    @GetMapping
    public String listarUsuarios(Model model, @RequestParam(required = false) String rolNombre, @RequestParam(required = false) String activoStr) {
        Boolean activo = (activoStr != null && !activoStr.isEmpty()) ? Boolean.parseBoolean(activoStr) : null;
        model.addAttribute("usuarios", usuarioService.findByFilters(rolNombre, activo));
        model.addAttribute("roles", rolService.findAll());
        return "admin/usuarios/lista_usuarios";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("usuarioForm", new UsuarioRegistroDTO());
        model.addAttribute("roles", rolService.findAll());
        return "admin/usuarios/form_usuario";
    }

    @PostMapping
    public String crearUsuario(@Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }
        try {
            usuarioService.crearUsuarioDesdeDTO(usuarioForm);
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            Usuario usuario = usuarioService.findById(id);
            UsuarioRegistroDTO usuarioForm = new UsuarioRegistroDTO();
            usuarioForm.setUserName(usuario.getUserName());
            usuarioForm.setEmail(usuario.getEmail());
            usuarioForm.setNombres(usuario.getNombres());
            usuarioForm.setApellidos(usuario.getApellidos());
            usuarioForm.setTelefono(usuario.getTelefono());
            usuarioForm.setDireccion(usuario.getDireccion());
            usuarioForm.setFechaNacimiento(usuario.getFechaNacimiento());
            if (usuario.getRol() != null) {
                usuarioForm.setRolId(usuario.getRol().getId());
            }
            usuarioForm.setDescripcion(usuario.getDescripcion());
            model.addAttribute("usuarioForm", usuarioForm);
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/{id}")
    public String actualizarUsuario(@PathVariable Long id, 
                                    @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm, // 👈 LE QUITAMOS EL @Valid
                                    BindingResult result, 
                                    Model model) {
        
        // Colocamos este print temporal para que veas en tu consola de Render si el formulario responde
        System.out.println("🚀 ¡PROCESANDO EDICIÓN! Recibido teléfono: " + usuarioForm.getTelefono());

        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }
        
        try {
            Usuario usuarioExistente = usuarioService.findById(id);
            
            // Actualización de campos
            usuarioExistente.setUserName(usuarioForm.getUserName());
            usuarioExistente.setEmail(usuarioForm.getEmail());
            usuarioExistente.setNombres(usuarioForm.getNombres());
            usuarioExistente.setApellidos(usuarioForm.getApellidos());
            usuarioExistente.setTelefono(usuarioForm.getTelefono()); // Ahora sí se guardará
            usuarioExistente.setDireccion(usuarioForm.getDireccion());
            usuarioExistente.setFechaNacimiento(usuarioForm.getFechaNacimiento());
            
            // Lógica de Contraseña: Solo cambia si el admin digitó una nueva
            if (usuarioForm.getPassword() != null && !usuarioForm.getPassword().trim().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioForm.getPassword()));
            }
            
            Rol rol = rolService.findByIdOrThrow(usuarioForm.getRolId());
            
            if (rol.getNombre().toUpperCase().contains("ENTRENADOR")) {
                usuarioExistente.setDescripcion(usuarioForm.getDescripcion());
            } else {
                usuarioExistente.setDescripcion(null);
            }
            
            usuarioExistente.setRol(rol);
            usuarioService.save(usuarioExistente);
            
            System.out.println("✅ ¡Usuario actualizado con éxito en la Base de Datos!");
            return "redirect:/admin/usuarios";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Error al actualizar: " + e.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try { 
            usuarioService.deleteById(id); 
            redirectAttributes.addFlashAttribute("mensaje", "✅ Usuario eliminado con éxito.");
        } catch (RuntimeException e) { 
            redirectAttributes.addFlashAttribute("error", "❌ Error al eliminar: " + e.getMessage()); 
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/aprobar/{id}")
    public String aprobarEntrenador(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null && "ROLE_ENTRENADOR".equals(usuario.getRol().getNombre()) && !usuario.getValidado()) {
            usuario.setValidado(true);
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "¡Entrenador aprobado!");
        }
        return "redirect:/admin/usuarios";
    }

    
    @GetMapping("/ver-cv-mongo/{idMongo}")
    @ResponseBody
    public ResponseEntity<byte[]> verCvMongo(@PathVariable String idMongo) {
        byte[] pdfBytes = pdfService.obtenerPdfDeMongo(idMongo);
        
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Cambiamos 'inline' por un nombre de archivo limpio y agregamos headers de seguridad
        headers.add("Content-Disposition", "inline; filename=\"CV_Entrenador.pdf\"");
        headers.add("X-Content-Type-Options", "nosniff"); // Evita que el navegador adivine el tipo de archivo

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}