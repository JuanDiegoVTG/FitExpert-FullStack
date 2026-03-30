package com.proyecto.emilite.controller.admin;

import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.dto.PagoFormDTO; // Asegúrate de tener este DTO
import com.proyecto.emilite.service.PagoService;
import com.proyecto.emilite.service.UsuarioService;
import com.proyecto.emilite.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("pagoAdminController")
@RequestMapping("/admin/pagos") // Path base para operaciones de ADMIN sobre pagos
public class PagoAdminController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioService servicioService;

    // Endpoint: GET /admin/pagos
    // Propósito: Mostrar la lista de pagos (solo para ADMIN)
    @GetMapping
    public String listarPagos(Model model) {
        List<Pago> pagos = pagoService.findAll();
        model.addAttribute("pagos", pagos);
        return "admin/pagos/lista_pagos"; // Vista para la lista (debes crearla o actualizarla)
    }

    // Endpoint: GET /admin/pagos/nuevo
    // Propósito: Mostrar el formulario para crear un nuevo pago (solo para ADMIN)
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("pagoForm", new PagoFormDTO()); // Objeto vacío para el formulario
        // Cargar listas para los selects (usuarios, servicios, promociones)
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        // model.addAttribute("promociones", promocionService.findAll()); // Añade si tienes PromocionService
        return "admin/pagos/form_pago"; // Vista para el formulario de creación
    }

    // Endpoint: POST /admin/pagos
    // Propósito: Procesar el formulario de creación de un nuevo pago (solo para ADMIN)
    @PostMapping
    public String crearPago(@Valid @ModelAttribute("pagoForm") PagoFormDTO pagoForm,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            // Recargar listas para los selects
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("servicios", servicioService.findAll());
            // model.addAttribute("promociones", promocionService.findAll()); // Añade si tienes PromocionService
            return "admin/pagos/form_pago";
        }

        try {
            // Llamar al servicio para crear el pago
            pagoService.crearPagoDesdeDTO(pagoForm);
            // Si la creación es exitosa, redirige a la lista de pagos
            return "redirect:/admin/pagos";
        } catch (Exception e) {
            // Manejo de errores inesperados
            model.addAttribute("error", "Error al crear el pago: " + e.getMessage());
            // Recargar listas para los selects
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("servicios", servicioService.findAll());
            // model.addAttribute("promociones", promocionService.findAll()); // Añade si tienes PromocionService
            return "admin/pagos/form_pago";
        }
    }

    // Endpoint: GET /admin/pagos/{id}/editar
    // Propósito: Mostrar el formulario para editar un pago existente (solo para ADMIN)
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Pago pago = pagoService.findById(id);
        if (pago == null) {
            throw new RuntimeException("Pago no encontrado con ID: " + id);
        }

        // Convertir la entidad Pago a DTO para el formulario
        PagoFormDTO pagoForm = new PagoFormDTO();
        pagoForm.setUsuarioId(pago.getUsuario().getId());
        pagoForm.setServicioId(pago.getServicio() != null ? pago.getServicio().getId() : null);
        // pagoForm.setPromocionId(pago.getPromocion() != null ? pago.getPromocion().getId() : null);
        pagoForm.setMonto(pago.getMonto());
        pagoForm.setMetodoPago(pago.getMetodoPago());
        pagoForm.setEstado(pago.getEstado());
        pagoForm.setReferenciaPago(pago.getReferenciaPago());

        model.addAttribute("pagoForm", pagoForm);
        model.addAttribute("pagoId", id); // Pasar el ID para el POST de edición
        // Cargar listas para los selects (usuarios, servicios, promociones)
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        // model.addAttribute("promociones", promocionService.findAll()); // Añade si tienes PromocionService
        return "admin/pagos/form_pago"; // Vista para el formulario de edición
    }

    // Endpoint: POST /admin/pagos/{id}
    // Propósito: Procesar el formulario de edición de un pago existente (solo para ADMIN)
    @PostMapping("/{id}")
    public String actualizarPago(@PathVariable Long id,
                                 @Valid @ModelAttribute("pagoForm") PagoFormDTO pagoForm,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pagoId", id);
            // Recargar listas para los selects
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("servicios", servicioService.findAll());
            // model.addAttribute("promociones", promocionService.findAll()); // Añade si tienes PromocionService
            // Si hay errores de validación, vuelve al formulario con los errores
            return "admin/pagos/form_pago";
        }

        Pago pagoExistente = pagoService.findById(id);

        // Actualizar la entidad con los datos del DTO
        // Asignar usuario
        Usuario usuario = usuarioService.findById(pagoForm.getUsuarioId());
        pagoExistente.setUsuario(usuario);

        // Asignar servicio (si se proporcionó)
        if (pagoForm.getServicioId() != null) {
                Servicio servicio = servicioService.findById(pagoForm.getServicioId());
                pagoExistente.setServicio(servicio);
        } else {
            pagoExistente.setServicio(null); // Limpiar si se dejó en blanco
        }


        pagoExistente.setMonto(pagoForm.getMonto());
        pagoExistente.setMetodoPago(pagoForm.getMetodoPago());
        pagoExistente.setEstado(pagoForm.getEstado());
        pagoExistente.setReferenciaPago(pagoForm.getReferenciaPago());

        // Guardar el pago actualizado
        pagoService.save(pagoExistente);

        // Redirigir a la lista de pagos después de actualizar
        return "redirect:/admin/pagos";
    }

    // Endpoint: POST /admin/pagos/{id}/eliminar
    // Propósito: Eliminar un pago existente (solo para ADMIN)
    @PostMapping("/{id}/eliminar")
    public String eliminarPago(@PathVariable Long id) {
        pagoService.deleteById(id);
        // Redirigir a la lista de pagos después de eliminar
        return "redirect:/admin/pagos";
    }
}