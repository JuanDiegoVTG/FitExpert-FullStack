package com.proyecto.emilite.controller;

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
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Progreso;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
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
            String json = objectMapper.writeValueAsString(payload);
            String respuesta = pythonService.enviarMensajeChat(json); 
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error de mensajería");
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
            // 1. OBTENER EL USUARIO ACTUAL (Resuelve: usuarioActual)
            Usuario usuarioActual = usuarioRepository.findByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. PREPARAR DATOS PARA PYTHON
            Map<String, Object> datos = new HashMap<>();
            datos.put("peso", peso);
            datos.put("altura", altura);
            datos.put("cuello", cuello);
            datos.put("cintura", cintura);
            datos.put("cadera", cadera != null ? cadera : 0.0);
            datos.put("sexo", usuarioActual.getPerfil().getSexo()); // M o F
            datos.put("edad", usuarioActual.getPerfil().getEdad());
            datos.put("nivel_actividad", nivelActividad);

            // 3. LLAMAR A LA IA (Aquí obtenemos los resultados)
            // Asumiendo que pythonService.generarDiagnostico devuelve un Map o un objeto con los datos
            Map<String, Object> diagnosticoIA = pythonService.obtenerDiagnosticoDesdePython(datos);

            // EXTRAER VALORES (Resuelve: grasaCalculada e imcCalculado)
            // Asegúrate de que los nombres coincidan con lo que devuelve tu JSON de Python
            Double grasaCalculada = Double.parseDouble(diagnosticoIA.get("grasa_corporal").toString());
            Double imcCalculado = Double.parseDouble(diagnosticoIA.get("imc").toString());

            // 4. GUARDAR EN LA NUEVA TABLA DE PROGRESO (Punto A a Punto B)
            Progreso nuevoProgreso = new Progreso();
            nuevoProgreso.setUsuario(usuarioActual);
            nuevoProgreso.setPeso(peso);
            nuevoProgreso.setGrasa(grasaCalculada);
            nuevoProgreso.setImc(imcCalculado);
            
            progresoRepository.save(nuevoProgreso); // ¡Aquí se guarda el historial!

            // 5. ACTUALIZAR EL PERFIL ACTUAL (Para que siempre tenga lo último)
            Perfil perfil = usuarioActual.getPerfil();
            perfil.setPeso(peso);
            perfil.setAltura(altura);
            perfil.setCintura(cintura);
            perfil.setCuello(cuello);
            perfil.setCadera(cadera);
            perfilRepository.save(perfil);

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