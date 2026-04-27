package com.proyecto.emilite.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.proyecto.emilite.service.RolService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/usuarios") 
public class AdminUsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mostrar la lista de usuarios (solo para ADMIN), con opción de filtrar
    @GetMapping
    public String listarUsuarios(Model model,
                                 @RequestParam(required = false) String rolNombre,
                                 @RequestParam(required = false) String activoStr) {

        // Convertir el string "true/false" a Boolean
        Boolean activo = null;
        if (activoStr != null && !activoStr.isEmpty()) {
            activo = Boolean.parseBoolean(activoStr);
        }

        // Obtener usuarios filtrados o todos si no hay filtros
        List<Usuario> usuarios = usuarioService.findByFilters(rolNombre, activo);

        // Cargar la lista de todos los roles para el formulario de filtro
        List<Rol> roles = rolService.findAll();

        // Añadir datos al modelo para la vista
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("filtroRol", rolNombre);
        model.addAttribute("filtroActivo", activoStr);
        return "admin/usuarios/lista_usuarios";
    }

    // Mostrar el formulario para crear un nuevo usuario (solo para ADMIN)
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("usuarioForm", new UsuarioRegistroDTO());
        model.addAttribute("roles", rolService.findAll());
        return "admin/usuarios/form_usuario";
    }

    
    // Procesar el formulario de creación de un nuevo usuario (solo para ADMIN)
    @PostMapping
    public String crearUsuario(@Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }

        try {
            usuarioService.crearUsuarioDesdeDTO(usuarioForm);
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el usuario: " + e.getMessage());
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }
    }

   
    //Mostrar el formulario para editar un usuario existente (solo para ADMIN)
   @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            // Usamos el método findById que devuelve Usuario o lanza una excepción
            Usuario usuario = usuarioService.findById(id); 

            // Convertir la entidad Usuario a DTO para el formulario
            UsuarioRegistroDTO usuarioForm = new UsuarioRegistroDTO();
            usuarioForm.setUserName(usuario.getUserName());
            usuarioForm.setEmail(usuario.getEmail());
            usuarioForm.setNombres(usuario.getNombres());
            usuarioForm.setApellidos(usuario.getApellidos());
            usuarioForm.setTelefono(usuario.getTelefono());
            usuarioForm.setDireccion(usuario.getDireccion());
            usuarioForm.setFechaNacimiento(usuario.getFechaNacimiento());
            usuarioForm.setRolId(usuario.getRol().getId()); 
            usuarioForm.setDescripcion(usuario.getDescripcion());

            model.addAttribute("usuarioForm", usuarioForm);
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        } catch (RuntimeException e) { 
            // Añadimos un mensaje de error al modelo
            model.addAttribute("error", e.getMessage()); 
            // Redirigimos a la lista de usuarios
            return "redirect:/admin/usuarios";
        }
    }

    //  Procesar el formulario de edición de un usuario existente (solo para ADMIN)
    @PostMapping("/{id}")
    public String actualizarUsuario(@PathVariable Long id,
                                   @Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioForm,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", rolService.findAll());
            return "admin/usuarios/form_usuario";
        }

        try {
            // Buscamos el usuario existente. Este método lanza una excepción si no lo encuentra.
            Usuario usuarioExistente = usuarioService.findById(id); 

            // Actualizar la entidad con los datos del DTO
            usuarioExistente.setUserName(usuarioForm.getUserName());
           
            usuarioExistente.setEmail(usuarioForm.getEmail());
            usuarioExistente.setNombres(usuarioForm.getNombres());
            usuarioExistente.setApellidos(usuarioForm.getApellidos());
            usuarioExistente.setTelefono(usuarioForm.getTelefono());
            usuarioExistente.setDireccion(usuarioForm.getDireccion());
            usuarioExistente.setFechaNacimiento(usuarioForm.getFechaNacimiento());
            usuarioExistente.setDescripcion(usuarioForm.getDescripcion());

            // Actualizar rol
                Rol rol = rolService.findByIdOrThrow(usuarioForm.getRolId());
            

            usuarioExistente.setRol(rol);

            // Guardar el usuario actualizado
            usuarioService.save(usuarioExistente);

            // Redirigir a la lista de usuarios después de actualizar
            return "redirect:/admin/usuarios";

        } catch (RuntimeException e) { 
             model.addAttribute("error", e.getMessage()); 
             model.addAttribute("usuarioId", id); 
             model.addAttribute("roles", rolService.findAll()); 
             return "admin/usuarios/form_usuario"; 
        }
    }
   

    
    // Eliminar un usuario existente (solo para ADMIN)
   @PostMapping("/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, Model model) {
        try {
            usuarioService.deleteById(id); 
        } catch (RuntimeException e) {
            
            model.addAttribute("error", e.getMessage()); 
            
        }

        // Redirigir a la lista de usuarios después de intentar eliminar
        return "redirect:/admin/usuarios";
    }

    
    @GetMapping("/aprobar/{id}") 
    public String aprobarEntrenador(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        // 1. Buscamos al usuario en la BD
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        
        // 2. Debug rápido (Mira tu consola de Linux, ahí verás qué nombre de rol está llegando)
        if (usuario != null) {
            System.out.println("DEBUG: Rol encontrado -> " + usuario.getRol().getNombre());
        }

        // 3. Condición corregida con "ROLE_ENTRENADOR"
        if (usuario != null && "ROLE_ENTRENADOR".equals(usuario.getRol().getNombre())) {
            
            usuario.setValidado(true);
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", "¡Entrenador aprobado!");
        } else {
            // Esto te ayuda a saber si falló el IF
            System.out.println("DEBUG: No se cumplió la condición de aprobación.");
        }
        
        return "redirect:/admin/usuarios"; // Redirige a la lista para ver el cambio
    }

    @GetMapping("/ver-cv/{nombreArchivo}")
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> servirCv(@PathVariable String nombreArchivo) {
        try {
            Path ruta = Paths.get("/home/juand/uploads/cvs/").resolve(nombreArchivo);
            org.springframework.core.io.Resource recurso = new org.springframework.core.io.UrlResource(ruta.toUri());

            if (recurso.exists() || recurso.isReadable()) {
                return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(recurso);
            }
        } catch (Exception e) {
            // Manejar error
        }
        return ResponseEntity.notFound().build();
    }
}