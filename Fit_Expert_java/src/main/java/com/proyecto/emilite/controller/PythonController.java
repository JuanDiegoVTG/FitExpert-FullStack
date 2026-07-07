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

@SuppressWarnings("null")
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
            @RequestParam String sexo,
            Authentication auth, 
            RedirectAttributes redirectAttributes) { // Usamos RedirectAttributes para pasar datos al GET

        try {
            // 1. Buscamos al usuario logueado
            Usuario usuarioActual = usuarioRepository.findByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. Manejo del Perfil
            Perfil perfil = usuarioActual.getPerfil();
            if (perfil == null) {
                perfil = new Perfil();
                perfil.setUsuario(usuarioActual);
                perfil.setNombreCompleto(usuarioActual.getNombres() + " " + usuarioActual.getApellidos());
            }

            // 3. Actualizamos datos
            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setCintura(cintura);
            perfil.setCuello(cuello);
            perfil.setCadera(cadera != null ? cadera : 0.0);
            perfil.setNivelActividad(nivelActividad);
            perfil.setSexo(sexo);

            // 4. Calculamos edad
            int edadCalculada = 0;
            if (usuarioActual.getFechaNacimiento() != null) {
                edadCalculada = java.time.Period.between(
                    usuarioActual.getFechaNacimiento(), 
                    java.time.LocalDate.now()
                ).getYears();
            }
            perfil.setEdad(edadCalculada);
            perfilRepository.save(perfil);

            // 5. Llamada al servicio de Python
            Map<String, Object> datosIA = new HashMap<>();
            datosIA.put("peso", peso);
            datosIA.put("altura", altura);
            datosIA.put("cuello", cuello);
            datosIA.put("cintura", cintura);
            datosIA.put("cadera", perfil.getCadera());
            datosIA.put("nivel_actividad", nivelActividad);
            datosIA.put("sexo", sexo);
            datosIA.put("edad", edadCalculada);

            Map<String, Object> diagnosticoIA = pythonService.obtenerDiagnosticoDesdePython(datosIA);

            // 6. Guardar en Historial
            Progreso nuevoProgreso = new Progreso();
            nuevoProgreso.setUsuario(usuarioActual);
            nuevoProgreso.setPeso(peso);
            nuevoProgreso.setGrasa(Double.parseDouble(diagnosticoIA.get("grasa_corporal").toString()));
            nuevoProgreso.setImc(Double.parseDouble(diagnosticoIA.get("imc").toString()));
            progresoRepository.save(nuevoProgreso);

            // 7. Usamos FlashAttributes para pasar el resultado al GET de forma segura
            redirectAttributes.addFlashAttribute("diagnostico", diagnosticoIA);
            redirectAttributes.addFlashAttribute("perfil", perfil);
            
            return "redirect:/valoracion/resultado";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/valoracion/inicial?error=true";
        }
    }

    @GetMapping("/valoracion/resultado")
    public String mostrarResultado() {
        // Thymeleaf detectará automáticamente los atributos del FlashAttribute
        return "cliente/resultado_diagnostico";
    }

    @GetMapping("/valoracion")
    public String mostrarValoracion(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Perfil perfil = usuario.getPerfil();
        if (perfil == null) perfil = new Perfil();
        
        model.addAttribute("perfil", perfil);
        return "cliente/valoracion_inicial";
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