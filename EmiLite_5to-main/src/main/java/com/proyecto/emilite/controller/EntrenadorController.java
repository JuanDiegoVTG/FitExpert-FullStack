package com.proyecto.emilite.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.RutinaFormDTO;
import com.proyecto.emilite.service.RutinaService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/entrenador")
public class EntrenadorController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    /**
     * Muestra la lista de clientes vinculados al entrenador actual.
     * Sincronizado con: mis_clientes.html usando ${usuarios}
     */
    @GetMapping("/clientes")
    public String verClientes(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario entrenador = usuarioService.findByUserName(username);
        
        // Filtramos para obtener solo los clientes que tienen este entrenador asignado
        List<Usuario> misClientes = usuarioService.findByEntrenadorId(entrenador.getId());
        
        // IMPORTANTE: El nombre del atributo debe ser "usuarios" para que el HTML lo lea
        model.addAttribute("usuarios", misClientes); 
        return "entrenador/mis_clientes"; 
    }

    /**
     * Muestra las rutinas de los clientes que pertenecen a este entrenador.
     * Sincronizado con: ver_rutinas.html usando ${rutinas}
     */
    @GetMapping("/rutinas")
    public String verRutinas(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario entrenador = usuarioService.findByUserName(username);
        
        List<Rutina> rutinas = rutinaService.findByEntrenadorId(entrenador.getId());
        
        model.addAttribute("rutinas", rutinas);
        return "entrenador/ver_rutinas"; 
    }

    @GetMapping("/rutinas/nueva")
    public String mostrarFormularioCreacionRutina(Model model) {
        model.addAttribute("rutinaForm", new RutinaFormDTO());
        
        // Cargamos todos los clientes con rol CLIENTE para la selección inicial
        List<Usuario> todosLosClientes = usuarioService.findByRolNombre("CLIENTE");
        model.addAttribute("clientes", todosLosClientes);
        
        return "entrenador/form_rutina";
    }

    /**
     * Procesa la creación de rutina y fuerza la vinculación Entrenador-Cliente
     */
    @PostMapping("/rutinas")
    public String crearRutina(@Valid @ModelAttribute("rutinaForm") RutinaFormDTO rutinaForm,
                              BindingResult result,
                              Authentication auth,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
            return "entrenador/form_rutina";
        }

        try {
            Usuario entrenadorActual = usuarioService.findByUserName(auth.getName());
            Usuario cliente = usuarioService.findByIdOrThrow(rutinaForm.getClienteId());

            // 🚀 VINCULACIÓN FORZADA: 
            // Cada vez que un entrenador crea una rutina para un cliente, 
            // ese cliente queda vinculado a él para aparecer en su lista de "Mis Clientes"
            cliente.setEntrenador(entrenadorActual);
            usuarioService.save(cliente);

            rutinaService.crearRutinaDesdeDTO(rutinaForm);
            return "redirect:/entrenador/rutinas";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear la rutina: " + e.getMessage());
            model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
            return "entrenador/form_rutina";
        }
    }

    @GetMapping("/rutinas/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Authentication auth, Model model) {
        Rutina rutina = rutinaService.findByIdOrThrow(id);

        RutinaFormDTO rutinaForm = new RutinaFormDTO();
        rutinaForm.setNombre(rutina.getNombre());
        rutinaForm.setDescripcion(rutina.getDescripcion());
        rutinaForm.setTipo(rutina.getTipo());
        rutinaForm.setNivelDificultad(rutina.getNivelDificultad());
        rutinaForm.setDuracionSemanas(rutina.getDuracionSemanas());
        rutinaForm.setActiva(rutina.getActivo());
        rutinaForm.setClienteId(rutina.getCliente().getId());

        model.addAttribute("rutinaForm", rutinaForm);
        model.addAttribute("rutinaId", id);
        model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
        return "entrenador/form_rutina";
    }

    /**
     * Actualiza la rutina y mantiene/asegura el vínculo con el entrenador
     */
    @PostMapping("/rutinas/{id}")
    public String actualizarRutina(@PathVariable Long id,
                                   @Valid @ModelAttribute("rutinaForm") RutinaFormDTO rutinaForm,
                                   BindingResult result,
                                   Authentication auth,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("rutinaId", id);
            model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
            return "entrenador/form_rutina";
        }

        Rutina rutinaExistente = rutinaService.findByIdOrThrow(id);
        rutinaExistente.setNombre(rutinaForm.getNombre());
        rutinaExistente.setDescripcion(rutinaForm.getDescripcion());
        rutinaExistente.setTipo(rutinaForm.getTipo());
        rutinaExistente.setNivelDificultad(rutinaForm.getNivelDificultad());
        rutinaExistente.setDuracionSemanas(rutinaForm.getDuracionSemanas());
        rutinaExistente.setActivo(rutinaForm.getActiva());

        Usuario cliente = usuarioService.findByIdOrThrow(rutinaForm.getClienteId());
        Usuario entrenadorActual = usuarioService.findByUserName(auth.getName());

        // Aseguramos que el cliente siga vinculado a este entrenador
        cliente.setEntrenador(entrenadorActual);
        usuarioService.save(cliente);

        rutinaExistente.setCliente(cliente);
        rutinaService.save(rutinaExistente);
        
        return "redirect:/entrenador/rutinas";
    }

    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable Long id) {
        java.util.Optional<Rutina> rutinaOpt = rutinaService.findById(id);
        if (rutinaOpt.isPresent()) {
            rutinaService.deleteById(id);
        }
        return "redirect:/entrenador/rutinas";
    }
}