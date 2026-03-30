package com.proyecto.emilite.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;

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
}