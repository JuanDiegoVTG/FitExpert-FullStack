package com.proyecto.emilite.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.PerfilRepository;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PythonService;
import com.proyecto.emilite.service.UsuarioService;

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
    private UsuarioService usuarioService;

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
            Usuario usuario = usuarioRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Perfil perfil = usuario.getPerfil();
            if (perfil == null) {
                perfil = new Perfil();
                perfil.setUsuario(usuario);
            }

            if (perfil.getNombreCompleto() == null || perfil.getNombreCompleto().isEmpty()) {
                perfil.setNombreCompleto(usuario.getNombres() + " " + usuario.getApellidos());
            }

            if (perfil.getEdad() == null) {
                perfil.setEdad(20); 
            }

            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setObjetivo(objetivo);
            
            perfilRepository.save(perfil);

            Map<String, Object> data = new HashMap<>();
            data.put("peso", peso);
            data.put("altura", altura);
            data.put("objetivo", objetivo);

            String jsonPayload = new ObjectMapper().writeValueAsString(data);
            String jsonRespuestaFlask = pythonService.generarRutina(jsonPayload);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rutinaGenerada = mapper.readValue(
                jsonRespuestaFlask, 
                new TypeReference<Map<String, Object>>() {}
            );

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

            rutinaRepository.save(nuevaRutina);
            model.addAttribute("rutina", nuevaRutina); 

            return "cliente/rutina";
            
        } catch (Exception e) {
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

    /**
     * MÉTODOS DEL CHAT (VISTAS Y LOGICA)
     */

    @GetMapping("/chat/{id}")
    public String verChat(@PathVariable Long id, Model model, Authentication auth) {
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        model.addAttribute("chatId", id);
        model.addAttribute("usuarioId", usuarioActual.getId());

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
            List<Usuario> alumnos = usuarioService.obtenerClientesDeEntrenador(usuarioActual.getId());
            model.addAttribute("clientes", alumnos);
            
            Usuario alumnoActual = usuarioRepository.findById(id).orElse(null);
            model.addAttribute("nombreCliente", alumnoActual != null ? alumnoActual.getNombres() + " " + alumnoActual.getApellidos() : "Alumno");
            
            return "entrenador/chat";
        }

        if (usuarioActual.getEntrenador() != null) {
            model.addAttribute("nombreEntrenador", "Prof. " + usuarioActual.getEntrenador().getNombres());
        } else {
            model.addAttribute("nombreEntrenador", "Entrenador Asignado");
        }
        
        return "cliente/chat";
    }

    @GetMapping("/chat/bandeja")
    public String abrirBandejaEntrada(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorUsername(auth.getName());
        
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
            List<Usuario> alumnos = usuarioService.obtenerClientesDeEntrenador(usuario.getId());
            if (!alumnos.isEmpty()) {
                return "redirect:/api/chat/" + alumnos.get(0).getId();
            }
            return "redirect:/dashboard?error=sin_alumnos";
        }
        
        return "redirect:/api/chat/" + usuario.getId();
    }

    @GetMapping("/chat/mensajes/{id}")
    @ResponseBody
    public ResponseEntity<String> cargarMensajes(@PathVariable Long id) {
        try {
            String mensajes = pythonService.obtenerMensajes(id);
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("[]");
        }
    }

    @PostMapping("/chat/enviar")
    @ResponseBody
    public ResponseEntity<String> enviarMensaje(@RequestBody Map<String, Object> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            String respuesta = pythonService.enviarMensajeChat(json); 
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error de mensajería");
        }
    }
}