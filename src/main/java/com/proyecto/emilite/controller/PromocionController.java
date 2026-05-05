package com.proyecto.emilite.controller;

import com.proyecto.emilite.model.Promocion;
import com.proyecto.emilite.model.dto.PromocionFormDTO;
import com.proyecto.emilite.service.PromocionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/promociones") 
public class PromocionController {

    @Autowired
    private PromocionService promocionService;


    // Mostrar la lista de promociones
    @GetMapping
    public String listarPromociones(Model model) {
        List<Promocion> promociones = promocionService.findAll();
        model.addAttribute("promociones", promociones);
        return "promociones/lista_promociones"; // Vista para la lista
    }

    
    // Mostrar el formulario para crear una nueva promoción
    @GetMapping("/nueva")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("promocionForm", new PromocionFormDTO()); 
        return "promociones/form_promocion"; // Vista para el formulario de creación
    }

    
    // Procesar el formulario de creación de una nueva promoción
    @PostMapping
    public String crearPromocion(@Valid @ModelAttribute("promocionForm") PromocionFormDTO promocionForm,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            return "promociones/form_promocion";
        }

        // Verificar si el código ya existe 
        if (promocionService.existsByCodigo(promocionForm.getCodigo())) {
            result.rejectValue("codigo", "error.promocionForm", "Ya existe una promoción con este código.");
            return "promociones/form_promocion";
        }

        // Crear la entidad Promocion desde el DTO
        Promocion nuevaPromocion = new Promocion();
        nuevaPromocion.setCodigo(promocionForm.getCodigo());
        nuevaPromocion.setDescripcion(promocionForm.getDescripcion());
        nuevaPromocion.setDescuentoPorcentaje(promocionForm.getDescuentoPorcentaje());
        nuevaPromocion.setFechaInicio(promocionForm.getFechaInicio());
        nuevaPromocion.setFechaFin(promocionForm.getFechaFin());
        nuevaPromocion.setMaxUsos(promocionForm.getMaxUsos());
        // Asignar 'activa' a true por defecto al crear
        nuevaPromocion.setActiva(true);
        // Inicializar usos actuales a 0
        nuevaPromocion.setUsosActuales(0);

        // Guardar la promoción
        promocionService.save(nuevaPromocion);

        // Redirigir a la lista de promociones después de crear
        return "redirect:/promociones";
    }


    // Mostrar el formulario para editar una promoción existente
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Promocion promocion = promocionService.findById(id);

        // Convertir la entidad Promocion a DTO para el formulario
        PromocionFormDTO promocionForm = new PromocionFormDTO();
        promocionForm.setCodigo(promocion.getCodigo());
        promocionForm.setDescripcion(promocion.getDescripcion());
        promocionForm.setDescuentoPorcentaje(promocion.getDescuentoPorcentaje());
        promocionForm.setFechaInicio(promocion.getFechaInicio());
        promocionForm.setFechaFin(promocion.getFechaFin());
        promocionForm.setMaxUsos(promocion.getMaxUsos());
        // Puedes incluir 'activa' si lo manejas en el form de edición

        model.addAttribute("promocionForm", promocionForm);
        model.addAttribute("promocionId", id); // Pasar el ID para el POST de edición
        return "promociones/form_promocion"; // Vista para el formulario de edición
    }

    
    // Procesar el formulario de edición de una promoción existente
    @PostMapping("/{id}")
    public String actualizarPromocion(@PathVariable Long id,
                                      @Valid @ModelAttribute("promocionForm") PromocionFormDTO promocionForm,
                                      BindingResult result,
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("promocionId", id);
            // Si hay errores de validación, vuelve al formulario con los errores
            return "promociones/form_promocion";
        }

        Promocion promocionExistente = promocionService.findById(id);

        // Verificar si el código cambió y si el nuevo código ya existe para otra promoción
        if (!promocionExistente.getCodigo().equals(promocionForm.getCodigo()) &&
            promocionService.existsByCodigo(promocionForm.getCodigo())) {
            result.rejectValue("codigo", "error.promocionForm", "Ya existe una promoción con este código.");
            return "promociones/form_promocion";
        }

        // Actualizar la entidad con los datos del DTO
        promocionExistente.setCodigo(promocionForm.getCodigo());
        promocionExistente.setDescripcion(promocionForm.getDescripcion());
        promocionExistente.setDescuentoPorcentaje(promocionForm.getDescuentoPorcentaje());
        promocionExistente.setFechaInicio(promocionForm.getFechaInicio());
        promocionExistente.setFechaFin(promocionForm.getFechaFin());
        promocionExistente.setMaxUsos(promocionForm.getMaxUsos());
        // Puedes actualizar 'activa' si lo manejas en el form de edición

        // Guardar la promoción actualizada
        promocionService.save(promocionExistente);

        // Redirigir a la lista de promociones después de actualizar
        return "redirect:/promociones";
    }

   
    //Eliminar una promoción existente
    @PostMapping("/{id}/eliminar")
    public String eliminarPromocion(@PathVariable Long id) {
        promocionService.deleteById(id);
        // Redirigir a la lista de promociones después de eliminar
        return "redirect:/promociones";
    }
}