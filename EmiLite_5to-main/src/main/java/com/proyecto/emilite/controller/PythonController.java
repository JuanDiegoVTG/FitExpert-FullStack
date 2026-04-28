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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.emilite.model.Mensaje;
import com.proyecto.emilite.model.Notificacion;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Progreso;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.MensajeRepository;
import com.proyecto.emilite.repository.NotificacionRepository;
import com.proyecto.emilite.repository.PerfilRepository;
import com.proyecto.emilite.repository.ProgresoRepository;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PythonService;
import com.proyecto.emilite.service.UsuarioService;


@Controller
@RequestMapping("/api") 
public class PythonController {

    private final PerfilRepository perfilRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Instancia única para eficiencia

    @Autowired
    private PythonService pythonService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    public PythonController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            Rutina rutina = rutinaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

            rutinaRepository.delete(rutina);
            flash.addFlashAttribute("success", "Rutina eliminada correctamente.");
            
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/cliente/rutinas";
    }

    /**
     * MÉTODOS DEL CHAT (VISTAS Y LOGICA)
     */
    @GetMapping("/chat/{usuarioDestinoId}")
    public String verChat(@PathVariable Long usuarioDestinoId, Model model, Authentication auth) {
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        
        // YA NO USAMOS Chat chat = chatService... 
        // Usamos el ID del usuario destino directamente como nuestro "chatId"
        model.addAttribute("chatId", usuarioDestinoId); 
        model.addAttribute("usuarioId", usuarioActual.getId());

        // El resto de tu lógica de Roles se queda igual
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
            model.addAttribute("clientes", usuarioService.obtenerClientesDeEntrenador(usuarioActual.getId()));
            Usuario alumnoActual = usuarioRepository.findById(usuarioDestinoId).orElse(null);
            model.addAttribute("nombreCliente", alumnoActual != null ? 
                            alumnoActual.getNombres() + " " + alumnoActual.getApellidos() : "Alumno");
            return "entrenador/chat";
        }

        // Lógica Cliente
        if (usuarioActual.getEntrenador() != null) {
            model.addAttribute("nombreEntrenador", "Prof. " + usuarioActual.getEntrenador().getNombres());
        }
        
        return "cliente/chat";
    }

    @GetMapping("/chat/bandeja")
    public String abrirBandejaEntrada(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorUsername(auth.getName());
        
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
            List<Usuario> alumnos = usuarioService.obtenerClientesDeEntrenador(usuario.getId());
            
            // ¡AGREGA ESTO PARA DEPURAR!
            System.out.println("DEBUG: Alumnos encontrados: " + (alumnos != null ? alumnos.size() : "null"));
            
            if (alumnos != null && !alumnos.isEmpty()) {
                return "redirect:/api/chat/" + alumnos.get(0).getId();
            }
            System.out.println("DEBUG: El entrenador no tiene alumnos o la lista está vacía.");
            return "redirect:/dashboard?error=sin_alumnos";
        }
        
        if (usuario.getEntrenador() != null) {
            // El cliente viaja al chat del ENTRENADOR (ej. ID 7)
            return "redirect:/api/chat/" + usuario.getEntrenador().getId();
        }
        
        return "redirect:/dashboard?error=sin_entrenador";
        
    }

    @GetMapping("/chat/mensajes/{idDestino}")
    @ResponseBody
    public List<Mensaje> cargarMensajes(@PathVariable Long idDestino, Authentication auth) {
        Usuario usuarioActual = usuarioService.obtenerPorUsername(auth.getName());
        // Busca en la base de datos MySQL los mensajes entre los dos
        return mensajeRepository.findChatMessages(usuarioActual.getId(), idDestino);
    }

    @PostMapping("/chat/enviar")
    @ResponseBody
    public ResponseEntity<String> enviarMensaje(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            String loginName = auth.getName(); 
            Usuario emisor = usuarioRepository.findByUserName(loginName)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado: " + loginName));

            Long idDestino = Long.valueOf(payload.get("chat_id").toString());
            String contenido = payload.get("content").toString();

            // 2. BUSCAR RECEPTOR
            Usuario receptor = usuarioRepository.findById(idDestino)
                    .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

            // 3. GUARDAR MENSAJE
            Mensaje msg = new Mensaje();
            msg.setSender(emisor);
            msg.setReceiver(receptor);
            msg.setContent(contenido);
            mensajeRepository.save(msg);

            // 4. CREAR NOTIFICACIÓN (Con fecha asegurada)
            Notificacion noti = new Notificacion();
            noti.setUsuario(receptor);
            noti.setMensaje("Nuevo mensaje de " + emisor.getNombres());
            noti.setLeida(false);
            noti.setFechaCreacion(LocalDateTime.now()); 

            notificacionRepository.save(noti);

            // Debug para consola de Java
            System.out.println("✅ Noti guardada para " + receptor.getUserName());

            // 5. LLAMAR A PYTHON
            String json = objectMapper.writeValueAsString(payload);
            String respuesta = pythonService.enviarMensajeChat(json); 
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error real en la consola
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/generar-diagnostico")
    public String generarDiagnostico(
            @RequestParam Double peso,
            @RequestParam Double altura,
            @RequestParam Double cuello,
            @RequestParam Double cintura,
            @RequestParam(required = false) Double cadera,
            @RequestParam String nivelActividad,
            Authentication auth, 
            Model model) {

        try {
            // 1. Obtener el usuario que está frente a la pantalla
            Usuario usuarioActual = usuarioRepository.findByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. Preparar datos y llamar a la IA (Python)
            Map<String, Object> datos = new HashMap<>();
            datos.put("peso", peso);
            datos.put("altura", altura);
            datos.put("cuello", cuello);
            datos.put("cintura", cintura);
            datos.put("cadera", cadera != null ? cadera : 0.0);
            datos.put("sexo", usuarioActual.getPerfil().getSexo()); 
            datos.put("edad", usuarioActual.getPerfil().getEdad());
            datos.put("nivel_actividad", nivelActividad);

            Map<String, Object> diagnosticoIA = pythonService.obtenerDiagnosticoDesdePython(datos);

            Double grasaCalculada = Double.parseDouble(diagnosticoIA.get("grasa_corporal").toString());
            Double imcCalculado = Double.parseDouble(diagnosticoIA.get("imc").toString());

            // 3. GUARDAR HISTORIAL (Punto A a Punto B)
            Progreso nuevoProgreso = new Progreso();
            nuevoProgreso.setUsuario(usuarioActual);
            nuevoProgreso.setPeso(peso);
            nuevoProgreso.setGrasa(grasaCalculada);
            nuevoProgreso.setImc(imcCalculado);
            progresoRepository.save(nuevoProgreso);

            // 4. ACTUALIZAR PERFIL ACTUAL
            Perfil perfil = usuarioActual.getPerfil();
            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setCintura(cintura);
            perfil.setCuello(cuello);
            perfil.setCadera(cadera);
            perfilRepository.save(perfil);

            // --- 🤖 AQUÍ EL TIMBRAZO PARA EL COACH ---
            // Si el cliente tiene entrenador, le avisamos de una vez
            if (usuarioActual.getEntrenador() != null) {
                Notificacion noti = new Notificacion();
                noti.setUsuario(usuarioActual.getEntrenador()); // El receptor es el Coach
                noti.setMensaje("📊 " + usuarioActual.getNombres() + " ha completado una valoración IA.");
                noti.setLeida(false);
                noti.setFechaCreacion(LocalDateTime.now());
                
                notificacionRepository.save(noti); // ¡AQUÍ se activa la campana del entrenador!
            }
            // ------------------------------------------

            model.addAttribute("diagnostico", diagnosticoIA);
            return "cliente/resultado_diagnostico";

        } catch (Exception e) {
            return "redirect:/api/valoracion?error=true";
        }
}
    @GetMapping("/valoracion")
    public String mostrarValoracion(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Le pasamos el perfil actual por si ya tiene datos guardados
        model.addAttribute("perfil", usuario.getPerfil());
        return "cliente/valoracion_inicial"; // Nombre de tu HTML del formulario
    }

    //CONTROL ENTRENADOR
    @GetMapping("/entrenador/cliente/{id}")
    public String verDetalleAlumno(@PathVariable Long id, Model model, Authentication auth) {
        // 1. Obtenemos el entrenador y LO USAMOS en el model (Adiós error)
        Usuario entrenador = usuarioService.obtenerPorUsername(auth.getName());
        model.addAttribute("entrenador", entrenador);
        
        // 2. Buscamos al alumno específico
        Usuario alumno = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        
        // Cargamos el historial del alumno para el Coach
        List<Progreso> historial = progresoRepository.findByUsuarioOrderByFechaRegistroAsc(alumno);
        // 3. Pasamos los datos del alumno y su perfil
        model.addAttribute("alumno", alumno);
        model.addAttribute("perfil", alumno.getPerfil());
        model.addAttribute("historial", historial); // Enviamos los puntos A, B, C...
        
        return "entrenador/detalle_alumno"; 
    }

}