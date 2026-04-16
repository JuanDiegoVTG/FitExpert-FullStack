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

    public PythonController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    /**
     * PROCESO DE GENERACIÓN DE RUTINA CON IA
     */
    @PostMapping("/rutina-real")
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
            
            Perfil perfil = usuario.getPerfil();
            if (perfil == null) {
                perfil = new Perfil();
                perfil.setUsuario(usuario);
            }

            // Si el nombre completo es obligatorio, lo sacamos del objeto Usuario
            if (perfil.getNombreCompleto() == null || perfil.getNombreCompleto().isEmpty()) {
                perfil.setNombreCompleto(usuario.getNombres() + " " + usuario.getApellidos());
            }

            // Si la edad es obligatoria y está nula, ponemos una por defecto (ej. 20 años)
            if (perfil.getEdad() == null) {
                perfil.setEdad(20); 
            }
            // --------------------------------------------------

            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setObjetivo(objetivo);
            
            // Guardamos el perfil sin que Hibernate se queje
            perfilRepository.save(perfil);

            // 2. Preparamos los datos para Flask
            Map<String, Object> data = new HashMap<>();
            data.put("peso", peso);
            data.put("altura", altura);
            data.put("objetivo", objetivo);

            String jsonPayload = new ObjectMapper().writeValueAsString(data);
            
            // 3. Llamamos al servicio de Python
            String jsonRespuestaFlask = pythonService.generarRutina(jsonPayload);

            // 4. Mapeamos la respuesta de la IA
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rutinaGenerada = mapper.readValue(
                jsonRespuestaFlask, 
                new TypeReference<Map<String, Object>>() {}
            );

            // 5. Creamos el objeto Rutina
            Rutina nuevaRutina = new Rutina(); 

            Object ejerciciosRaw = rutinaGenerada.get("ejercicios");
            if (ejerciciosRaw != null) {
                String ejerciciosLimpios = ejerciciosRaw.toString()
                    .replace("[", "").replace("]", "").replace("\"", "");
                nuevaRutina.setDescripcion(ejerciciosLimpios);
            } else {
                nuevaRutina.setDescripcion("Rutina personalizada generada por FitExpert IA");
            }

            String nombreIA = (String) rutinaGenerada.get("rutina");
            if (nombreIA == null) nombreIA = (String) rutinaGenerada.get("nombre_rutina");
            if (nombreIA == null) nombreIA = "Mi Plan FitExpert IA";
            
            nuevaRutina.setNombre(nombreIA);
            nuevaRutina.setTipo("IA Generada");
            nuevaRutina.setNivelDificultad("Personalizado");
            
            Object dias = rutinaGenerada.get("dias");
            nuevaRutina.setDuracionSemanas(dias instanceof Integer ? (Integer) dias : 4);
            
            nuevaRutina.setCliente(usuario); 

            // 6. Guardamos la rutina
            rutinaRepository.save(nuevaRutina);
            
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