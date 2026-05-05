package com.proyecto.emilite.controller.admin; // Carpeta específica para controladores de ADMIN

import com.proyecto.emilite.model.Promocion;
import com.proyecto.emilite.model.dto.PromocionFormDTO; // Asegúrate de tener este DTO
import com.proyecto.emilite.service.PromocionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("promocionAdminController")
@RequestMapping("/admin/promociones") // Path base para operaciones de ADMIN sobre promociones
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    // Endpoint: GET /admin/promociones
    // Propósito: Mostrar la lista de promociones (solo para ADMIN)
    @GetMapping
    public String listarPromociones(Model model) {
        List<Promocion> promociones = promocionService.findAll();
        model.addAttribute("promociones", promociones);
        return "admin/promociones/lista_promociones"; // Vista para la lista
    }

    // Endpoint: GET /admin/promociones/nueva
    // Propósito: Mostrar el formulario para crear una nueva promoción (solo para ADMIN)
    @GetMapping("/nueva")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("promocionForm", new PromocionFormDTO()); // Objeto vacío para el formulario
        return "admin/promociones/form_promocion"; // Vista para el formulario de creación
    }

    // Endpoint: POST /admin/promociones
    // Propósito: Procesar el formulario de creación de una nueva promoción (solo para ADMIN)
    @PostMapping
    public String crearPromocion(@Valid @ModelAttribute("promocionForm") PromocionFormDTO promocionForm,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            return "admin/promociones/form_promocion";
        }

        try {
            // Llamar al servicio para crear la promoción
            promocionService.crearPromocionDesdeDTO(promocionForm);
            // Si la creación es exitosa, redirige a la lista de promociones
            return "redirect:/admin/promociones";
        } catch (Exception e) {
            // Manejo de errores inesperados
            model.addAttribute("error", "Error al crear la promoción: " + e.getMessage());
            return "admin/promociones/form_promocion";
        }
    }

    // Endpoint: GET /admin/promociones/{id}/editar
    // Propósito: Mostrar el formulario para editar una promoción existente (solo para ADMIN)
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        // --- CORREGIDO: No usar orElseThrow ---
        Promocion promocion = promocionService.findById(id);
        if (promocion == null) {
            // Si no se encuentra la promoción, lanzar una excepción o redirigir a una página de error
            throw new RuntimeException("Promoción no encontrada con ID: " + id);
            // Opcional: return "redirect:/admin/promociones?error=promocion_no_encontrada";
        }
        // --- FIN CORRECCIÓN ---

        // Convertir la entidad Promocion a DTO para el formulario
        PromocionFormDTO promocionForm = new PromocionFormDTO();
        promocionForm.setCodigo(promocion.getCodigo());
        promocionForm.setDescripcion(promocion.getDescripcion());
        promocionForm.setDescuentoPorcentaje(promocion.getDescuentoPorcentaje());
        promocionForm.setFechaInicio(promocion.getFechaInicio());
        promocionForm.setFechaFin(promocion.getFechaFin());
        promocionForm.setMaxUsos(promocion.getMaxUsos());
        promocionForm.setActiva(promocion.getActiva()); // <-- Este getter debe existir en Promocion.java

        model.addAttribute("promocionForm", promocionForm);
        model.addAttribute("promocionId", id); // Pasar el ID para el POST de edición
        return "admin/promociones/form_promocion"; // Vista para el formulario de edición
    }

    // Endpoint: POST /admin/promociones/{id}
    // Propósito: Procesar el formulario de edición de una promoción existente (solo para ADMIN)
    @PostMapping("/{id}")
    public String actualizarPromocion(@PathVariable Long id,
                                     @Valid @ModelAttribute("promocionForm") PromocionFormDTO promocionForm,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("promocionId", id);
            // Si hay errores de validación, vuelve al formulario con los errores
            return "admin/promociones/form_promocion";
        }

        // --- CORREGIDO: No usar orElseThrow ---
        Promocion promocionExistente = promocionService.findById(id);
        if (promocionExistente == null) {
             // Si no se encuentra la promoción, lanzar una excepción o redirigir a una página de error
             throw new RuntimeException("Promoción no encontrada con ID: " + id);
             // Opcional: return "redirect:/admin/promociones?error=promocion_no_encontrada_para_edicion";
        }
        // --- FIN CORRECCIÓN ---

        // Actualizar la entidad con los datos del DTO
        promocionExistente.setCodigo(promocionForm.getCodigo()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setDescripcion(promocionForm.getDescripcion()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setDescuentoPorcentaje(promocionForm.getDescuentoPorcentaje()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setFechaInicio(promocionForm.getFechaInicio()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setFechaFin(promocionForm.getFechaFin()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setMaxUsos(promocionForm.getMaxUsos()); // <-- Este getter debe existir en PromocionFormDTO.java
        promocionExistente.setActiva(promocionForm.getActiva()); // <-- Este getter debe existir en PromocionFormDTO.java

        // Guardar la promoción actualizada
        promocionService.save(promocionExistente);

        // Redirigir a la lista de promociones después de actualizar
        return "redirect:/admin/promociones";
    }

    // Endpoint: POST /admin/promociones/{id}/eliminar
    // Propósito: Eliminar una promoción existente (solo para ADMIN)
    @PostMapping("/{id}/eliminar")
    public String eliminarPromocion(@PathVariable Long id) {
        // Opcional: Verificar si la promoción existe antes de eliminarla
        // Promocion promocion = promocionService.findById(id);
        // if (promocion == null) {
        //     return "redirect:/admin/promociones?error=promocion_no_encontrada_para_eliminacion";
        // }
        promocionService.deleteById(id);
        // Redirigir a la lista de promociones después de eliminar
        return "redirect:/admin/promociones";
    }
}