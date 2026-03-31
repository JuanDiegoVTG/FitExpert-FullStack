package com.proyecto.emilite.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PythonService;



@Controller
@RequestMapping("/api") 
public class PythonController {

    @Autowired
    private PythonService pythonService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @GetMapping("/rutina-real")
    public String rutinaReal(Authentication auth, Model model) {

        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Ups, no encontré al usuario"));

        Perfil perfil = usuario.getPerfil();

        if (perfil == null) {
            throw new RuntimeException("El usuario aún no tiene un perfil creado");
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("peso", perfil.getPeso());
            data.put("altura", perfil.getAltura());
            data.put("objetivo", perfil.getObjetivo());

            String json = new ObjectMapper().writeValueAsString(data);

            System.out.println("Enviando datos de: " + username);
            System.out.println("Datos que van para Flask: " + json);

            String jsonRespuestaFlask = pythonService.generarRutina(json);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rutinaGenerada = mapper.readValue(
                jsonRespuestaFlask, 
                new TypeReference<Map<String, Object>>() {}
            );

            // 1. CREAMOS EL OBJETO 
            Rutina nuevaRutina = new Rutina(); 

            // 2. VALIDACIÓN SEGURA DEL NOMBRE
            // Intentamos buscar "rutina" y si no está, buscamos "nombre_rutina"
            String nombreDesdeFlask = (String) rutinaGenerada.get("rutina"); 
            if (nombreDesdeFlask == null || nombreDesdeFlask.trim().isEmpty()) {
                nombreDesdeFlask = (String) rutinaGenerada.get("nombre_rutina");
            }
            if (nombreDesdeFlask == null || nombreDesdeFlask.trim().isEmpty()) {
                nombreDesdeFlask = "Rutina Personalizada IA"; 
            }
            nuevaRutina.setNombre(nombreDesdeFlask);

            // 3. RECUPERAMOS LOS DEMÁS DATOS DE FORMA SEGURA
            Object ejercicios = rutinaGenerada.get("ejercicios");
            nuevaRutina.setDescripcion(ejercicios != null ? "Ejercicios: " + ejercicios.toString() : "Ejercicios personalizados por IA");
            
            nuevaRutina.setTipo("Generada por IA");
            nuevaRutina.setNivelDificultad("Media"); 
            
            Object dias = rutinaGenerada.get("dias");
            if (dias instanceof Integer) {
                nuevaRutina.setDuracionSemanas((Integer) dias);
            } else {
                nuevaRutina.setDuracionSemanas(4); // Si Flask no envía días, ponemos 4 por defecto
            }
            
            nuevaRutina.setCliente(usuario); // Asignamos el dueño de la rutina

            // 4. GUARDAMOS Y ENVIAMOS A LA VISTA
            rutinaRepository.save(nuevaRutina);
            model.addAttribute("rutinaGeneradaIA", nuevaRutina);

            return "cliente/rutina";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cliente/dashboard";
        }
    }

    @PostMapping("/rutinas/{id}/favorita")
    public String marcarFavorita(@PathVariable Long id) {
        Rutina rutinaEncontrada = rutinaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se ha enconrtado el ID de la rutina"));
        
        rutinaEncontrada.setEs_favorita(true);

        rutinaRepository.save(rutinaEncontrada);

        return "redirect:/cliente/dashboard";
    }

    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarFavorita(@PathVariable Long id){

    rutinaRepository.deleteById(id);
    return "redirect:/cliente/rutinas";
        
    }   
}