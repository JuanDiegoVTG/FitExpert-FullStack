package com.proyecto.emilite.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PythonService;

@RestController
@RequestMapping("/api") // Esta es la ruta base para entrar a estas funciones
public class PythonController {

    // Traemos las herramientas que necesitamos: el servicio de Python y la base de datos de usuarios
    @Autowired
    private PythonService pythonService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/rutina-real")
    public String rutinaReal(Authentication auth) {

        // Identificamos quién es el usuario que está usando la app ahora mismo
        String username = auth.getName();

        // Buscamos toda su información en la base de datos usando su nombre
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Ups, no encontré al usuario"));

        // Extraemos su perfil 
        Perfil perfil = usuario.getPerfil();

        if (perfil == null) {
            throw new RuntimeException("El usuario aún no tiene un perfil creado");
        }

        try {
            // Preparamos el "paquete" de datos para enviárselo a Python
            // Metemos el peso, la altura y el objetivo en un mapa (como una lista de etiquetas)
            Map<String, Object> data = new HashMap<>();
            data.put("peso", perfil.getPeso());
            data.put("altura", perfil.getAltura());
            data.put("objetivo", perfil.getObjetivo());

            // Convertimos esa lista de datos en un texto tipo JSON (que es lo que Python entiende)
            String json = new ObjectMapper().writeValueAsString(data);

            // Estos mensajes solo salen en la consola para nosotros saber que todo va bien
            System.out.println("Enviando datos de: " + username);
            System.out.println("Datos que van para Flask: " + json);

            // Le mandamos el paquete al microservicio de Flask y esperamos su respuesta
            // La respuesta será la rutina que Python generó
            return pythonService.generarRutina(json);

        } catch (Exception e) {
            // Si algo falla (por ejemplo, si Flask está apagado), avisamos del error
            e.printStackTrace();
            return "Lo siento, hubo un error al conectar con el generador de rutinas";
        }
    }
}