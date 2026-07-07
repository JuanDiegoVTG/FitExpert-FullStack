package com.proyecto.emilite.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PythonService {

    @Autowired
    private RestTemplate restTemplate; // Usaremos siempre este inyectado

    @Value("${api.ia.url}")
    private String flaskUrl;

    public Double validarCvConPython(MultipartFile file, String username) {
        try {
            String url = flaskUrl + "/validar-cv";
            System.out.println("🚀 Conectando con servicio IA en: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 🔥 SOLUCIÓN NUBE: Envoltura segura del archivo en bytes con su nombre original
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv.pdf";
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            body.add("file", fileResource); 
            body.add("username", username); 

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Petición al servicio de Flask
            String respuesta = restTemplate.postForObject(url, requestEntity, String.class);
            System.out.println("🐍 Respuesta cruda de Python: " + respuesta);
            
            if (respuesta != null) {
                JsonNode root = new ObjectMapper().readTree(respuesta);
                // Retorna el score si existe, de lo contrario da 0.0
                return root.path("score").asDouble(0.0);
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR AL VALIDAR CV CON PYTHON: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }


    // --- MÉTODOS DE CHAT ---
    public String obtenerMensajes(Long chatId) {
        String url = flaskUrl + "/get_messages/" + chatId;
        return restTemplate.getForObject(url, String.class);
    }

    public String enviarMensajeChat(String jsonPayload) {
        String url = flaskUrl + "/send_message";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
        return restTemplate.postForObject(url, request, String.class);
    }

    public String generarDiagnostico(Map<String, Object> payload) {
        // Usamos flaskUrl y la ruta EXACTA de Python
        String url = flaskUrl + "/api/generar-diagnostico"; 
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Python: " + e.getMessage());
        }
    }

    /**
     * Envía los datos antropométricos a Flask y recibe el diagnóstico de la IA.
     */
    /**
 * Envía los datos antropométricos a Flask y recibe el diagnóstico de la IA.
 * Incluye reintentos automáticos ante 429 (típico de un cold start en Render free tier).
 */
    public Map<String, Object> obtenerDiagnosticoDesdePython(Map<String, Object> datos) {
        String url = flaskUrl;
        int maxIntentos = 3;
        int esperaMs = 5000;

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
                );

                return response.getBody();

            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                System.out.println("⚠️ Intento " + intento + "/" + maxIntentos
                    + ": Flask devolvió 429 (probable cold start). Reintentando en " + (esperaMs / 1000) + "s...");

                if (intento == maxIntentos) {
                    throw new RuntimeException(
                        "El servicio de IA no respondió tras " + maxIntentos + " intentos (posible cold start prolongado)."
                    );
                }

                try {
                    Thread.sleep(esperaMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Reintento interrumpido: " + ie.getMessage());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error al conectar: " + e.getMessage());
            }
        }

        // Este punto es inalcanzable en la práctica (el for siempre retorna o lanza),
        // pero se deja por completitud del compilador.
        throw new RuntimeException("No se pudo obtener diagnóstico tras reintentos.");
    }
}