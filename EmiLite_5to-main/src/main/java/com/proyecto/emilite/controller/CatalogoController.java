package com.proyecto.emilite.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.service.UsuarioService;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * 1. Muestra la vista del catálogo
     */
    @GetMapping
    public String catalogoCliente(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "calificacion", required = false) Integer calificacion,
            Model model) {

        List<Usuario> entrenadores;

        if (keyword != null && !keyword.isEmpty()) {
            entrenadores = usuarioService.buscarPorNombreOEspecialidad(keyword);
        } else {
            entrenadores = usuarioService.findByRolNombre("ENTRENADOR");
        }

        model.addAttribute("entrenadores", entrenadores);
        model.addAttribute("keyword", keyword);
        return "cliente/catalogo";
    }

    /**
     * 2. API para Mercado Pago (Lo que el JS llama con fetch)
     * He puesto la ruta como /crear-preferencia dentro de /catalogo
     */
    @PostMapping("/crear-preferencia")
    @ResponseBody
    public ResponseEntity<?> crearPreferencia(@RequestBody Map<String, Object> data) {
        try {
            // 1. Validación de Seguridad de Datos
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El cuerpo de la solicitud está vacío.");
            }

            Object idObj = data.get("entrenadorId");
            Object precioObj = data.get("precio");
            
            if (idObj == null || precioObj == null) {
                return ResponseEntity.badRequest().body("Error: Falta 'entrenadorId' o 'precio' en la solicitud.");
            }

            String entrenadorId = idObj.toString();
            String titulo = data.getOrDefault("titulo", "Suscripción FitExpert").toString();
            
            // 2. Manejo seguro del precio
            BigDecimal precio;
            try {
                precio = new BigDecimal(precioObj.toString());
            } catch (NumberFormatException e) {
                precio = new BigDecimal("50000"); // Precio de respaldo si el formato falla
            }

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                precio = new BigDecimal("50000"); // Evita el error de Mercado Pago de precio <= 0
            }

            // 3. Configuración de Mercado Pago
            // Asegúrate de que este Token sea el vigente en tu Dashboard
            MercadoPagoConfig.setAccessToken("TEST-7552848406438918-041404-5baedc52fb2dfbada51b2ba66e978576-685191240");

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(titulo)
                    .quantity(1)
                    .unitPrice(precio)
                    .currencyId("COP")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8082/catalogo/pago-exitoso")
                    .failure("http://localhost:8082/catalogo")
                    .pending("http://localhost:8082/catalogo")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .externalReference(entrenadorId) 
                    //.autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 4. Respuesta Exitosa
            Map<String, String> response = new HashMap<>();
            response.put("init_point", preference.getInitPoint());
            
            return ResponseEntity.ok(response);

        } catch (com.mercadopago.exceptions.MPApiException apiEx) {
            // BLINDAJE: Aquí capturamos el error REAL que devuelve Mercado Pago (JSON)
            System.err.println("--- ERROR DE API MERCADO PAGO ---");
            System.err.println("Status: " + apiEx.getApiResponse().getStatusCode());
            System.err.println("Content: " + apiEx.getApiResponse().getContent());
            return ResponseEntity.status(400).body("Error de Mercado Pago: " + apiEx.getApiResponse().getContent());

        } catch (Exception e) {
            // Error genérico del servidor
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/pago-exitoso")
        public String pagoExitoso(
                @RequestParam("payment_id") String paymentId,
                @RequestParam("status") String status,
                @RequestParam("external_reference") String entrenadorId, 
                Model model) {

            // 1. Buscamos al entrenador para mostrar sus datos en la confirmación
            Long id = Long.parseLong(entrenadorId);
            Usuario entrenador = usuarioService.findByIdOrThrow(id);

            // 2. Lógica de negocio: Aquí Kevin marcaba al usuario como Premium 
            // o creaba la relación en la base de datos.
            // usuarioService.activarSuscripcion(entrenadorId); 

            model.addAttribute("paymentId", paymentId);
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("status", status);

            return "cliente/pago-exitoso";
        }
}