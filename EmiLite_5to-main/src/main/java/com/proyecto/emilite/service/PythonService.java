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

@Service // Le dice a Spring que este es un servicio de lógica
public class PythonService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Esta función es el "mensajero" que viaja hasta Python
    public String generarRutina(String json) {
        // RestTemplate es la herramienta para hacer llamadas a otras páginas o APIs
        RestTemplate restTemplate = new RestTemplate();

        // La dirección donde está prendido nuestro programa de Python (Flask)
        String url = "http://127.0.0.1:8000/generar-rutina";

        // Avisamos que el paquete que enviamos es de tipo JSON (texto estructurado)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Metemos el contenido (json) y las etiquetas (headers) en un sobre
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        // Hacemos el envío (POST) y recibimos la respuesta de Python
        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                request,
                String.class
        );

        // Sacamos lo que Python escribió dentro de la respuesta y lo devolvemos
        return response.getBody();
    }

    //  Esta función prepara todo antes de llamar al mensajero
    public String generarRutinaDesdePerfil(Authentication auth) {
        // Obtenemos el nombre del usuario que está usando la app
        String username = auth.getName();

        // Buscamos sus datos completos en la base de datos
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("No encontré al usuario"));

        // Miramos su perfil físico (peso, altura, etc.)
        Perfil perfil = usuario.getPerfil();

        if (perfil == null) {
            throw new RuntimeException("El perfil está vacío");
        }

        try {
            // Organizamos los datos del perfil en una lista (Mapa)
            Map<String, Object> data = new HashMap<>();
            data.put("peso", perfil.getPeso());
            data.put("altura", perfil.getAltura());
            data.put("objetivo", perfil.getObjetivo());

            // Convertimos esa lista a formato JSON (texto que Python entiende)
            String json = new ObjectMapper().writeValueAsString(data);

            // Ahora sí, llamamos a la función de arriba para que le mande esto a Python
            return generarRutina(json);

        } catch (Exception e) {
            // Si el cable se corta o Python está apagado, mostramos el error
            e.printStackTrace();
            return "Error al conectar con el servidor de rutinas";
        }
    }

    private final String PYTHON_URL = "http://localhost:8000/validar-cv";

    public Double validarCvConPython(MultipartFile file, String username) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Configuramos las cabeceras (Headers)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 2. Creamos el cuerpo de la petición (El "paquete")
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Convertimos el MultipartFile a algo que RestTemplate entienda
            body.add("file", file.getResource()); 
            body.add("username", username); // Podemos mandar datos extra

            // 3. Armamos la petición completa
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4. Disparamos hacia Flask
            // Recibimos la respuesta en un String para ver qué dijo la IA
            String respuesta = restTemplate.postForObject(PYTHON_URL, requestEntity, String.class);
            
            // 5. Convertimos el String de respuesta en un objeto que Java entienda
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(respuesta);
            
            // Extraemos el valor de la llave "score" que viene en el JSON
            Double scoreExtraido = root.path("score").asDouble();

            System.out.println("Respuesta de Python: " + respuesta);
            return scoreExtraido;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 0.0; // Si algo falla, devolvemos 0 para no romper el flujo
        }
       
    }
}