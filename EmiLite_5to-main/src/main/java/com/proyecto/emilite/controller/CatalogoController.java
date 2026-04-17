package com.proyecto.emilite.controller;

import java.math.BigDecimal;
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

            // 1. Armamos las URLs
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8082/catalogo/pago-exitoso")
                    .failure("http://localhost:8082/catalogo")
                    .pending("http://localhost:8082/catalogo")
                    .build();

            // 2. Metemos al paquete
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest)) // (Asegúrate de que tu variable se llame itemRequest o items)
                    .backUrls(backUrls) // <--- ¡MP se estaba quejando de que no encontraba esto!
                    //.autoReturn("approved")
                    .externalReference(String.valueOf(entrenadorId)) 
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 4. Respuesta Exitosa
            Map<String, String> response = new HashMap<>();
            response.put("init_point", preference.getInitPoint());
            response.put("id", preference.getId());
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

    // --- 1. MUESTRA LA PANTALLA Y RECIBE LOS DATOS DE MP ---
    @GetMapping("/pago-exitoso")
    public String mostrarPantallaExito(
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "external_reference", required = false) String externalReference,
            Model model) {
        
        System.out.println("Llegó a la pantalla de éxito. ID Profe: " + externalReference);
        
        // 1. Pasar el ID del entrenador al HTML para el botón
        model.addAttribute("entrenadorId", externalReference);
        
        // 2. Pasar el ID del pago que nos da Mercado Pago (para que se vea bonito en el recibo)
        model.addAttribute("paymentId", paymentId != null ? paymentId : "Pendiente/Prueba");

        // 3. Buscar al entrenador en la BD para mostrar su nombre en el recibo
        if (externalReference != null) {
            try {
                Long id = Long.parseLong(externalReference);
                // NOTA: Ajusta el nombre de este método según como lo tengas en tu UsuarioService
                Usuario entrenador = usuarioService.findById(id); 
                model.addAttribute("entrenador", entrenador);
            } catch (Exception e) {
                System.out.println("Error buscando entrenador para la vista: " + e.getMessage());
            }
        }
        
        return "cliente/pago-exitoso"; 
    }

    // --- 2. HACE LA MAGIA EN LA BD (Recibe el clic de tu botón) ---
    @PostMapping("/activar-entrenador")
    public String activarEntrenador(
            @RequestParam("entrenadorId") Long entrenadorId, 
            Authentication auth) {
        
        try {
            // 1. Obtener el email del cliente que está logueado
            String emailCliente = auth.getName();
            
            // 2. Buscar al Cliente y al Entrenador en la base de datos
            Usuario cliente = usuarioService.findByEmail(emailCliente); 
            Usuario entrenador = usuarioService.findById(entrenadorId); 
            
            // 3. Conectarlos y Guardar
            if (cliente != null && entrenador != null) {
                cliente.setEntrenador(entrenador);
                // NOTA: Ajusta este método al que uses para actualizar/guardar en tu Service/Repository
                usuarioService.save(cliente); 
                System.out.println("¡ÉXITO! Cliente " + cliente.getNombres() + " asignado al entrenador " + entrenador.getNombres());
            }

        } catch (Exception e) {
            System.err.println("Error al enlazar en la BD: " + e.getMessage());
        }
        
        // 4. Redirigimos al chat para que empiecen a hablar
        return "redirect:/api/chat/" + entrenadorId;
    }
}