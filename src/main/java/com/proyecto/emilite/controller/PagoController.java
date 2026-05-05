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
import java.math.RoundingMode; // Importante para el Fix de BigDecimal
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/pagos")
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
    @ResponseBody // 🔥 CRÍTICO: Para que el JS reciba el ID y no intente cargar una página HTML
    public String crearPreferencia(@PathVariable Long id) {
        try {
            // 1. ⚡ CREDENCIALES COHERENTES (TOKEN TEST)
            // Debe empezar por TEST- para que coincida con tu Public Key del HTML
            MercadoPagoConfig.setAccessToken("APP_USR-4856297003007933-040323-ab7c7dac806d261fd8528f4c510bb4aa-3311074257");

            Pago pago = pagoService.findById(id);
            if (pago == null) return "error_no_existe";

            // 2. CONFIGURACIÓN DEL ÍTEM (Fix de BigDecimal depreciado)
            String nombreServicio = (pago.getServicio() != null) ? pago.getServicio().getNombre() : "Aporte FitExpert";
            
            // Usamos RoundingMode.HALF_UP para evitar el error tachado en tu IDE
            BigDecimal montoFinal = new BigDecimal(pago.getMonto().toString())
                                    .setScale(2, RoundingMode.HALF_UP);

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pago.getId().toString())
                    .title(nombreServicio)
                    .quantity(1)
                    .unitPrice(montoFinal)
                    .currencyId("COP")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // 3. BACK URLS (Donde vuelve el cliente)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:8082/admin/pagos")
                .pending("http://localhost:8082/admin/pagos")
                .failure("http://localhost:8082/admin/pagos")
                .build();

            // 4. PREFERENCE REQUEST
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                //.autoReturn("approved")
                .externalReference("PAGO_" + pago.getId()) // Sin espacios para evitar errores 400
                .build();

            // 5. LLAMADA A MERCADO PAGO
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 6. LOG DE ÉXITO Y RETORNO
            System.out.println("✅ Preferencia Generada: " + preference.getId());
            return preference.getId();

        } catch (com.mercadopago.exceptions.MPApiException apiEx) {
            // 🕵️ EL CHISMOSO: Imprime el error exacto de Mercado Pago en la consola de Java
            System.err.println("❌ ERROR VALIDACIÓN MP: " + apiEx.getApiResponse().getContent());
            return "error_mp";
        } catch (Exception e) {
            e.printStackTrace();
            return "error_interno";
        }
    }
    
}