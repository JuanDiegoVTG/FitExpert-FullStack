package com.proyecto.emilite.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;

@Service
public class PythonService {

    @Autowired
    private RestTemplate restTemplate; // Usaremos siempre este inyectado

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String FLASK_URL = "http://localhost:5000";

    public String generarRutina(String json) {
        String url = FLASK_URL + "/generar-rutina";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        
        // Usamos el restTemplate de la clase
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    public String generarRutinaDesdePerfil(Authentication auth) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new RuntimeException("No encontré al usuario"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) throw new RuntimeException("El perfil está vacío");

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("peso", perfil.getPeso());
            data.put("altura", perfil.getAltura());
            data.put("objetivo", perfil.getObjetivo());

            String json = new ObjectMapper().writeValueAsString(data);
            return generarRutina(json);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al conectar con el servidor de rutinas";
        }
    }

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
}