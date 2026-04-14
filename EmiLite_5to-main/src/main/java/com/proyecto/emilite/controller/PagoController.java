package com.proyecto.emilite.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import com.proyecto.emilite.dto.PreferenciaDTO;
import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.PagoRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.ContratoService;

@Controller
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired private PagoRepository pagoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    
    @Autowired private ContratoService contratoService;

    @PostMapping("/crear-preferencia")
    @ResponseBody
    public ResponseEntity<?> crearPreferencia(@RequestBody PreferenciaDTO dto, Authentication auth) {
        try {
            // 1. Configuración del Token 
            MercadoPagoConfig.setAccessToken("TEST-7552848406438918-041404-5baedc52fb2dfbada51b2ba66e978576-685191240");

            Usuario cliente = usuarioRepository.findByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. Validación de seguridad para el precio
            if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body("El precio debe ser mayor a cero.");
            }
            
            BigDecimal precioFinal = BigDecimal.valueOf(dto.getPrecio());

            // 3. Guardar el registro de pago en la BD
            Pago pago = new Pago();
            pago.setUsuario(cliente);
            pago.setMonto(precioFinal);
            pago.setEstado("PENDIENTE");
            pago.setMetodoPago("MERCADO_PAGO");
            pago.setReferenciaPago("FIT-" + System.currentTimeMillis());
            pagoRepository.save(pago);

            // 4. Crear el ítem (Mercado Pago exige una lista)
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(dto.getTitulo())
                    .quantity(1)
                    .unitPrice(precioFinal)
                    .currencyId("COP")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // 5. Configurar Back URLs con 127.0.0.1 (Truco para evitar el error de auto_return)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://127.0.0.1:8082/api/pagos/exito") 
                    .pending("http://127.0.0.1:8082/dashboard")
                    .failure("http://127.0.0.1:8082/dashboard")
                    .build();

            // 6. Construir la Preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    //.autoReturn("approved") 
                    .externalReference(pago.getId() + "-" + dto.getEntrenadorId())
                    .build();

            // 7. Crear en Mercado Pago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            
            // 8. Enviar respuesta al Front-end
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("init_point", preference.getInitPoint());
            
            System.out.println("✅ Link generado con auto-return: " + preference.getInitPoint());
            return ResponseEntity.ok(respuesta);

        } catch (com.mercadopago.exceptions.MPApiException apiEx) {
            String errorDetail = apiEx.getApiResponse().getContent();
            System.err.println("❌ Error de Mercado Pago: " + errorDetail);
            return ResponseEntity.status(apiEx.getApiResponse().getStatusCode()).body(errorDetail);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/exito")
    public String pagoExitoso(@RequestParam("status") String status, 
                            @RequestParam("external_reference") String ref, 
                            Authentication auth, 
                            Model model) {
        System.out.println("🚀 Entrando a /exito con status: " + status + " y ref: " + ref);
        
        if ("approved".equalsIgnoreCase(status)) {
            try {
                String[] ids = ref.split("-");
                Long pagoId = Long.parseLong(ids[0]);
                Long entrenadorId = Long.parseLong(ids[1]);
                System.out.println("✅ IDs obtenidos - Pago: " + pagoId + ", Entrenador: " + entrenadorId);

                Usuario cliente = usuarioRepository.findByUserName(auth.getName()).get();
                System.out.println("👤 Cliente encontrado: " + cliente.getUserName());

                Usuario entrenador = usuarioRepository.findById(entrenadorId).get();
                System.out.println("💪 Entrenador encontrado: " + entrenador.getUserName());

                pagoRepository.findById(pagoId).ifPresent(p -> {
                    p.setEstado("APROBADO");
                    pagoRepository.save(p);
                    System.out.println("💰 Pago actualizado a APROBADO en la BD");
                });

                // 🔍 AQUÍ ES DONDE PASA LA MAGIA
                contratoService.crearVínculo(cliente, entrenador);
                System.out.println("🔥 Vínculo creado - IA ACTIVADA");

                cliente.setEsPremuim(true); 
                usuarioRepository.save(cliente); 
                System.out.println("⭐ Usuario actualizado a PREMIUM en la BD");
                
                return "cliente/pago_exitoso";
            } catch (Exception e) {
                System.err.println("❌ ERROR EN EL PROCESO: " + e.getMessage());
                e.printStackTrace(); // Esto te dirá la línea exacta del error
                return "redirect:/dashboard?error=true";
            }
        }
        return "redirect:/dashboard";
    }
}