package com.proyecto.emilite.controller;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.service.PagoService;
import com.proyecto.emilite.service.UsuarioService;
import com.proyecto.emilite.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/pagos") // Mantenemos /admin/pagos para que coincida con tu SecurityConfig
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioService servicioService;

    // --- GESTIÓN ADMINISTRATIVA ---

    @GetMapping
    public String listarPagosAdmin(Model model) {
        model.addAttribute("pagos", pagoService.findAll());
        return "admin/pagos/lista_pagos"; 
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioPago(Model model) {
        model.addAttribute("pagoForm", new Pago());
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        return "admin/pagos/form_pago";
    }

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute("pagoForm") Pago pago) {
        pagoService.save(pago);
        return "redirect:/admin/pagos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("pagoForm", pagoService.findById(id));
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        return "admin/pagos/form_pago"; 
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarPago(@PathVariable Long id) {
        pagoService.deleteById(id);
        return "redirect:/admin/pagos";
    }

    // --- INTEGRACIÓN MERCADO PAGO ---

    @GetMapping("/crear-preferencia/{id}")
    @ResponseBody
    public String crearPreferencia(@PathVariable Long id) {
        try {
            // 1. CREDENCIALES: Usa el TOKEN DE PRUEBA
            MercadoPagoConfig.setAccessToken("APP_USR-4856297003007933-040323-ab7c7dac806d261fd8528f4c510bb4aa-3311074257");

            Pago pago = pagoService.findById(id);

            // 2. CONFIGURACIÓN DEL ÍTEM
            String nombreServicio = (pago.getServicio() != null) ? pago.getServicio().getNombre() : "Aporte FitExpert";
            
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pago.getId().toString())
                    .title(nombreServicio)
                    .quantity(1)
                    .unitPrice(new BigDecimal(pago.getMonto().toString()))
                    .currencyId("COP")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

           
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:8082/admin/pagos")
                .pending("http://localhost:8082/admin/pagos")
                .failure("http://localhost:8082/admin/pagos")
                .build();

        
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .externalReference(pago.getReferenciaPago())
                .build();

            //Crear la preferencia
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Retornamos el ID para que el frontend abra el Checkout
            return preference.getId();

        } catch (Exception e) {
            // Log detallado en consola si Mercado Pago rechaza la petición
            if (e instanceof com.mercadopago.exceptions.MPApiException) {
                System.err.println("--- ERROR DE MERCADO PAGO ---");
                System.err.println(((com.mercadopago.exceptions.MPApiException) e).getApiResponse().getContent());
                System.err.println("-----------------------------");
            }
            e.printStackTrace();
            return "error";
        }
    }
}