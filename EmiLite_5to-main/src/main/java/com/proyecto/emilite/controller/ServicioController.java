package com.proyecto.emilite.controller;

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

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    // Mostrar lista de servicios
    @GetMapping
    public String listarServicios(Model model) {
        List<Servicio> servicios = servicioService.findAll();
        model.addAttribute("servicios", servicios);
        // Usar vista de cliente existente
        return "cliente/ver_servicios";
    }

    // Formulario para crear nuevo servicio
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        // Esta acción es de administración; redirigir al controlador admin
        return "redirect:/admin/servicios/nuevo";
    }

    // Crear nuevo servicio
    @PostMapping
    public String crearServicio(@Valid @ModelAttribute("servicioForm") ServicioFormDTO servicioForm,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            return "redirect:/admin/servicios/nuevo";
        }

        Servicio nuevoServicio = new Servicio();
        nuevoServicio.setNombre(servicioForm.getNombre());
        nuevoServicio.setDescripcion(servicioForm.getDescripcion());
        nuevoServicio.setDuracionMinutos(servicioForm.getDuracionMinutos());
        nuevoServicio.setPrecio(servicioForm.getPrecio());
        nuevoServicio.setActivo(true);  // Activo por defecto

        servicioService.save(nuevoServicio);

        // Redirigir a la vista pública de servicios
        return "redirect:/servicios";
    }

    // Formulario para editar servicio
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {

        // YA NO DEVUELVE OPTIONAL — NO USAR orElseThrow aquí
        Servicio servicio = servicioService.findById(id);

        ServicioFormDTO servicioForm = new ServicioFormDTO();
        servicioForm.setNombre(servicio.getNombre());
        servicioForm.setDescripcion(servicio.getDescripcion());
        servicioForm.setDuracionMinutos(servicio.getDuracionMinutos());
        servicioForm.setPrecio(servicio.getPrecio());

        // Esta acción es de administración; redirigir al admin
        return "redirect:/admin/servicios/" + id + "/editar";
    }

    // Guardar edición de servicio
    @PostMapping("/{id}")
    public String actualizarServicio(@PathVariable Long id,
                                     @Valid @ModelAttribute("servicioForm") ServicioFormDTO servicioForm,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("servicioId", id);
            return "redirect:/admin/servicios/" + id + "/editar";
        }

        Servicio servicioExistente = servicioService.findById(id);

        servicioExistente.setNombre(servicioForm.getNombre());
        servicioExistente.setDescripcion(servicioForm.getDescripcion());
        servicioExistente.setDuracionMinutos(servicioForm.getDuracionMinutos());
        servicioExistente.setPrecio(servicioForm.getPrecio());

        servicioService.save(servicioExistente);

        return "redirect:/servicios";
    }

    // Eliminar servicio
    @PostMapping("/{id}/eliminar")
    public String eliminarServicio(@PathVariable Long id) {
        servicioService.deleteById(id);
        return "redirect:/servicios";
    }

    // Mostrar servicios activos al cliente
    @GetMapping("/cliente/servicios")
    public String verServiciosCliente(Model model) {
        List<Servicio> servicios = servicioService.findByActivo(true);
        model.addAttribute("servicios", servicios);
        return "cliente/ver_servicios";
    }
}
