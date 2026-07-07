package com.proyecto.emilite.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.*;
import com.proyecto.emilite.repository.*;
import com.proyecto.emilite.service.PythonService;
import com.proyecto.emilite.service.UsuarioService;

@SuppressWarnings("null")
@Controller
public class PythonController {

    private final PerfilRepository perfilRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private PythonService pythonService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProgresoRepository progresoRepository;
    @Autowired private RutinaRepository rutinaRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    public PythonController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    // --- DIAGNÓSTICO Y VALORACIÓN ---

    @GetMapping("/valoracion")
    public String mostrarValoracion(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Perfil perfil = usuario.getPerfil();
        model.addAttribute("perfil", perfil != null ? perfil : new Perfil());
        return "cliente/valoracion_inicial";
    }

    @PostMapping("/generar-diagnostico")
    public String generarDiagnostico(
            @RequestParam Double peso, @RequestParam Double altura,
            @RequestParam Double cuello, @RequestParam Double cintura,
            @RequestParam(required = false) Double cadera,
            @RequestParam String nivelActividad, @RequestParam String sexo,
            Authentication auth, RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioActual = usuarioRepository.findByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Perfil perfil = usuarioActual.getPerfil();
            if (perfil == null) {
                perfil = new Perfil();
                perfil.setUsuario(usuarioActual);
                perfil.setNombreCompleto(usuarioActual.getNombres() + " " + usuarioActual.getApellidos());
            }

            perfil.setPeso(peso); perfil.setAltura(altura); perfil.setCintura(cintura);
            perfil.setCuello(cuello); perfil.setCadera(cadera != null ? cadera : 0.0);
            perfil.setNivelActividad(nivelActividad); perfil.setSexo(sexo);

            int edadCalculada = (usuarioActual.getFechaNacimiento() != null) ? 
                java.time.Period.between(usuarioActual.getFechaNacimiento(), java.time.LocalDate.now()).getYears() : 0;
            
            perfil.setEdad(edadCalculada);
            perfilRepository.save(perfil);

            Map<String, Object> datosIA = new HashMap<>();
            datosIA.put("peso", peso); datosIA.put("altura", altura);
            datosIA.put("cuello", cuello); datosIA.put("cintura", cintura);
            datosIA.put("cadera", perfil.getCadera()); datosIA.put("nivel_actividad", nivelActividad);
            datosIA.put("sexo", sexo); datosIA.put("edad", edadCalculada);

            Map<String, Object> diagnosticoIA = pythonService.obtenerDiagnosticoDesdePython(datosIA);

            Progreso nuevoProgreso = new Progreso();
            nuevoProgreso.setUsuario(usuarioActual);
            nuevoProgreso.setPeso(peso);
            nuevoProgreso.setGrasa(Double.parseDouble(diagnosticoIA.get("grasa_corporal").toString()));
            nuevoProgreso.setImc(Double.parseDouble(diagnosticoIA.get("imc").toString()));
            progresoRepository.save(nuevoProgreso);

            redirectAttributes.addFlashAttribute("diagnostico", diagnosticoIA);
            redirectAttributes.addFlashAttribute("perfil", perfil);
            
            return "redirect:/valoracion/resultado";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/valoracion?error=true";
        }
    }

    @GetMapping("/valoracion/resultado")
    public String mostrarResultado() {
        return "cliente/resultado_diagnostico";
    }

    // --- RUTINAS Y CHAT ---

    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable("id") Long id, RedirectAttributes flash) {
        rutinaRepository.deleteById(id);
        flash.addFlashAttribute("success", "Rutina eliminada.");
        return "redirect:/cliente/rutinas";
    }

    @GetMapping("/chat/{usuarioDestinoId}")
    public String verChat(@PathVariable Long usuarioDestinoId, Model model, Authentication auth) {
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        model.addAttribute("chatId", usuarioDestinoId);
        model.addAttribute("usuarioId", usuarioActual.getId());
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR")) ? "entrenador/chat" : "cliente/chat";
    }

    @GetMapping("/chat/bandeja")
    public String abrirBandejaEntrada(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorUsername(auth.getName());
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
            List<Usuario> alumnos = usuarioService.obtenerClientesDeEntrenador(usuario.getId());
            return (!alumnos.isEmpty()) ? "redirect:/chat/" + alumnos.get(0).getId() : "redirect:/dashboard?error=sin_alumnos";
        }
        return (usuario.getEntrenador() != null) ? "redirect:/chat/" + usuario.getEntrenador().getId() : "redirect:/dashboard?error=sin_entrenador";
    }

    @PostMapping("/chat/enviar")
    @ResponseBody
    public ResponseEntity<String> enviarMensaje(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            Usuario emisor = usuarioService.obtenerPorUsername(auth.getName());
            Long idDestino = Long.valueOf(payload.get("chat_id").toString());
            Usuario receptor = usuarioRepository.findById(idDestino).orElseThrow();
            
            Mensaje msg = new Mensaje();
            msg.setSender(emisor); msg.setReceiver(receptor); msg.setContent(payload.get("content").toString());
            mensajeRepository.save(msg);
            
            Notificacion noti = new Notificacion();
            noti.setUsuario(receptor); noti.setMensaje("Nuevo mensaje de " + emisor.getNombres());
            noti.setFechaCreacion(LocalDateTime.now());
            notificacionRepository.save(noti);

            return ResponseEntity.ok(pythonService.enviarMensajeChat(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}