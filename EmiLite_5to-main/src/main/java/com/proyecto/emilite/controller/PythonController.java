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
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.PerfilRepository;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PythonService;



@Controller
@RequestMapping("/api") 
public class PythonController {

    private final PerfilRepository perfilRepository;

    @Autowired
    private PythonService pythonService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RutinaRepository rutinaRepository;


    PythonController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }


    @GetMapping("/rutina-real")
    public String rutinaReal(
        @RequestParam(name = "peso", defaultValue = "70") Double peso,     
        @RequestParam(name = "altura") Double altura,  
        @RequestParam(name = "objetivo") String objetivo,
        Authentication auth, Model model
    ){

        //Buscamos al usuario y su perfil
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Ups, no encontré al usuario"));
        Perfil perfil = usuario.getPerfil();

        // Si el usuario es nuevo y no tiene perfil, lo creamos de una vez
        if (perfil == null) {
            perfil = new Perfil();
            perfil.setUsuario(usuario); 
        }

        // Solo llenamos datos por defecto si están realmente vacíos
        if (perfil.getNombreCompleto() == null || perfil.getNombreCompleto().isEmpty()) {
        perfil.setNombreCompleto(usuario.getUserName());
        }
        if (perfil.getEdad() == null || perfil.getEdad() <= 0) {
            // Si ya tenía edad, dejamos la que tiene. Si no, ponemos 18.
            perfil.setEdad(18); 
        }
        //Actualizamos el perfil con los nuevos datos del formulario
        perfil.setPeso(peso);
        perfil.setAltura(altura);
        perfil.setObjetivo(objetivo);

        //Guardamos los datos en la BD
        perfilRepository.save(perfil);

        try {
            // Usamos las variables de arriba, ya no es necesario get()
            Map<String, Object> data = new HashMap<>();
            data.put("peso",  peso);
            data.put("altura", altura);
            data.put("objetivo", objetivo);

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

            //Limpieza de texto
            Object ejerciciosRaw = rutinaGenerada.get("ejercicios");
            if (ejerciciosRaw != null) {
                // Convertimos a String y quitamos los caracteres molestos [ ] "
                String ejerciciosLimpios = ejerciciosRaw.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "");
                    
                nuevaRutina.setDescripcion(ejerciciosLimpios);
            } else {
                nuevaRutina.setDescripcion("Ejercicios personalizados por IA");
            }

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
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/rutinas/{id}/favorita")
    public String marcarFavorita(@PathVariable Long id) {
        Rutina rutinaEncontrada = rutinaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se ha enconrtado el ID de la rutina"));
        
        rutinaEncontrada.setEs_favorita(true);

        rutinaRepository.save(rutinaEncontrada);

        return "redirect:/dashboard";
    }

    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarFavorita(@PathVariable Long id){

    rutinaRepository.deleteById(id);
    return "redirect:/cliente/rutinas";
        
    }
    
    
}