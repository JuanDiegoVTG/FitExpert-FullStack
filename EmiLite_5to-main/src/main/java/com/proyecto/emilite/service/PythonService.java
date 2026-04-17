package com.proyecto.emilite.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

@Service
public class PythonService {

    @Autowired
    private RestTemplate restTemplate; // Usaremos siempre este inyectado

    private final String FLASK_URL = "http://localhost:5000";


    public Double validarCvConPython(MultipartFile file, String username) {
        try {
            String url = FLASK_URL + "/validar-cv";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource()); 
            body.add("username", username); 

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String respuesta = restTemplate.postForObject(url, requestEntity, String.class);
            
            JsonNode root = new ObjectMapper().readTree(respuesta);
            return root.path("score").asDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    // --- MÉTODOS DE CHAT ---
    public String obtenerMensajes(Long chatId) {
        String url = FLASK_URL + "/get_messages/" + chatId;
        return restTemplate.getForObject(url, String.class);
    }

    public String enviarMensajeChat(String jsonPayload) {
        String url = FLASK_URL + "/send_message";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
        return restTemplate.postForObject(url, request, String.class);
    }

    public String generarDiagnostico(Map<String, Object> payload) {
        String url = "http://localhost:5000/api/diagnostico";
        
        // 1. CREAR LOS HEADERS (Esto es lo que falta)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Aquí está el truco

        // 2. EMPAQUETAR EL PAYLOAD CON LOS HEADERS
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // 3. ENVIAR EL POST
            return restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Python: " + e.getMessage());
        }
    }

    /**
     * Envía los datos antropométricos a Flask y recibe el diagnóstico de la IA.
     */
    public Map<String, Object> obtenerDiagnosticoDesdePython(Map<String, Object> datos) {
        // La URL de tu servidor Flask (asegúrate que el puerto sea el correcto)
        String url = "http://localhost:5000/api/diagnostico";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);

            // USAMOS exchange en lugar de postForEntity para manejar los genéricos <String, Object>
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {} // Esto le dice a Java: "Es un mapa de verdad"
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody(); // Ya no necesitas el (Map<String, Object>) porque ya viene tipado
            } else {
                throw new RuntimeException("Error en Python: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar con el servicio de IA: " + e.getMessage());
        }
    }
}