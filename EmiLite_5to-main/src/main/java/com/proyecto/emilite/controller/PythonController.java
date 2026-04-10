package com.proyecto.emilite.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import com.proyecto.emilite.service.ContratoService;
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

    @Autowired
    private ContratoService contratoService;

    public PythonController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    //PROCESO DE GENERACIÓN DE RUTINA CON IA
     
    @PostMapping("/rutina-real") // CAMBIADO A POST para coincidir con el form
    public String rutinaReal(
        @RequestParam(name = "peso", defaultValue = "70") Double peso,     
        @RequestParam(name = "altura") Double altura,  
        @RequestParam(name = "objetivo") String objetivo,
        Authentication auth, Model model
    ){
        String username = auth.getName();

        try {

            // 1. Buscamos al usuario
            Usuario usuario = usuarioRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // ¿Tiene un contrato activo con un entrenador?
            if (!contratoService.tieneEntrenadorActivo(usuario.getId())) {

                // Notificación satisfactoria 
                model.addAttribute("error", "Acceso denegado: Primero debes contratar a un entrenador para habilitar la IA.");

                // Lo mandamos al catálogo 
                return "redirect:/cliente/entrenadores?error=sin_contrato";
            }
            
            //2. Actualizamos su perfil
            Perfil perfil = usuario.getPerfil();
            if (perfil == null){
                perfil = new Perfil();
                perfil.setUsuario(usuario);
            }

            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setObjetivo(objetivo);
            perfilRepository.save(perfil);

            // 3. Preparamos los datos para Flask
            Map<String, Object> data = new HashMap<>();
            data.put("peso", peso);
            data.put("altura", altura);
            data.put("objetivo", objetivo);

            String jsonPayload = new ObjectMapper().writeValueAsString(data);
            
            // 4. Llamamos al servicio de Python
            String jsonRespuestaFlask = pythonService.generarRutina(jsonPayload);

            // 5. Mapeamos la respuesta de la IA
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rutinaGenerada = mapper.readValue(
                jsonRespuestaFlask, 
                new TypeReference<Map<String, Object>>() {}
            );

            // 6. Creamos el objeto Rutina para nuestra BD
            Rutina nuevaRutina = new Rutina(); 

            // Extraemos y limpiamos los ejercicios
            Object ejerciciosRaw = rutinaGenerada.get("ejercicios");
            if (ejerciciosRaw != null) {
                String ejerciciosLimpios = ejerciciosRaw.toString()
                    .replace("[", "").replace("]", "").replace("\"", "");
                nuevaRutina.setDescripcion(ejerciciosLimpios);
            } else {
                nuevaRutina.setDescripcion("Rutina personalizada generada por FitExpert IA");
            }

            // Extraemos el nombre de la rutina (validando llaves de Flask)
            String nombreIA = (String) rutinaGenerada.get("rutina");
            if (nombreIA == null) nombreIA = (String) rutinaGenerada.get("nombre_rutina");
            if (nombreIA == null) nombreIA = "Mi Plan FitExpert IA";
            
            nuevaRutina.setNombre(nombreIA);
            nuevaRutina.setTipo("IA Generada");
            nuevaRutina.setNivelDificultad("Personalizado");
            
            // Extraemos días
            Object dias = rutinaGenerada.get("dias");
            nuevaRutina.setDuracionSemanas(dias instanceof Integer ? (Integer) dias : 4);
            
            nuevaRutina.setCliente(usuario); 

            // 7.Guardamos y mandamos a la vista con el nombre "rutina"
            rutinaRepository.save(nuevaRutina);
            
            // Aquí corregimos el Error 500: el HTML busca ${rutina}
            model.addAttribute("rutina", nuevaRutina); 

            return "cliente/rutina";
            
        } catch (Exception e) {
            System.err.println("ERROR EN LA GENERACIÓN: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard?error=true";
        }
    }

    @PostMapping("/rutinas/{id}/favorita")
    public String marcarFavorita(@PathVariable Long id) {
        Rutina rutinaEncontrada = rutinaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ID de rutina no encontrado"));
        
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