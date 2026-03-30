package com.proyecto.emilite.controller.admin; 

import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.dto.ServicioFormDTO; 
import com.proyecto.emilite.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("servicioAdminController")
@RequestMapping("/admin/servicios") // Path base para operaciones de ADMIN sobre servicios
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    // Mostrar la lista de servicios (solo para ADMIN)
    @GetMapping
    public String listarServicios(Model model) {
        List<Servicio> servicios = servicioService.findAll();
        model.addAttribute("servicios", servicios);
        return "admin/servicios/lista_servicios"; // Vista para la lista
    }

    // Mostrar el formulario para crear un nuevo servicio (solo para ADMIN)
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("servicioForm", new ServicioFormDTO()); // Objeto vacío para el formulario
        return "admin/servicios/form_servicio"; // Vista para el formulario de creación
    }

    // Procesar el formulario de creación de un nuevo servicio (solo para ADMIN)
    @PostMapping
    public String crearServicio(@Valid @ModelAttribute("servicioForm") ServicioFormDTO servicioForm,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            return "admin/servicios/form_servicio";
        }

        try {
            // Llamar al servicio para crear el servicio
            servicioService.crearServicioDesdeDTO(servicioForm);
            // Si la creación es exitosa, redirige a la lista de servicios
            return "redirect:/admin/servicios";
        } catch (Exception e) {
            // Manejo de errores inesperados
            model.addAttribute("error", "Error al crear el servicio: " + e.getMessage());
            return "admin/servicios/form_servicio";
        }
    }

    
    // Mostrar el formulario para editar un servicio existente (solo para ADMIN)
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        
        Servicio servicio = servicioService.findById(id);
        if (servicio == null) {
            
            throw new RuntimeException("Servicio no encontrado con ID: " + id);
            
        }
      

        // Convertir la entidad Servicio a DTO para el formulario
        ServicioFormDTO servicioForm = new ServicioFormDTO();
        servicioForm.setNombre(servicio.getNombre());
        servicioForm.setDescripcion(servicio.getDescripcion());
        servicioForm.setDuracionMinutos(servicio.getDuracionMinutos());
        servicioForm.setPrecio(servicio.getPrecio());
        servicioForm.setActivo(servicio.getActivo());

        model.addAttribute("servicioForm", servicioForm);
        model.addAttribute("servicioId", id);
        return "admin/servicios/form_servicio"; 
    }

    // 
    // Procesar el formulario de edición de un servicio existente (solo para ADMIN)
    @PostMapping("/{id}")
    public String actualizarServicio(@PathVariable Long id,
                                    @Valid @ModelAttribute("servicioForm") ServicioFormDTO servicioForm,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
            model.addAttribute("servicioId", id);
            // Si hay errores de validación, vuelve al formulario con los errores
            return "admin/servicios/form_servicio";
        }


        Servicio servicioExistente = servicioService.findById(id);
        if (servicioExistente == null) {
             
             throw new RuntimeException("Servicio no encontrado con ID: " + id);
             
        }
       

        // Actualizar la entidad con los datos del DTO
        servicioExistente.setNombre(servicioForm.getNombre());
        servicioExistente.setDescripcion(servicioForm.getDescripcion());
        servicioExistente.setDuracionMinutos(servicioForm.getDuracionMinutos());
        servicioExistente.setPrecio(servicioForm.getPrecio());
        servicioExistente.setActivo(servicioForm.getActivo());

        // Guardar el servicio actualizado
        servicioService.save(servicioExistente);

        // Redirigir a la lista de servicios después de actualizar
        return "redirect:/admin/servicios";
    }

    // Eliminar un servicio existente (solo para ADMIN)
    @PostMapping("/{id}/eliminar")
    public String eliminarServicio(@PathVariable Long id) {
       
        servicioService.deleteById(id);
        // Redirigir a la lista de servicios después de eliminar
        return "redirect:/admin/servicios";
    }
}