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
@RequestMapping("/entrenador") // Path base para operaciones de ENTRENADOR
public class EntrenadorController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    // Endpoint: GET /entrenador/clientes
    // Propósito: Mostrar SOLO los clientes asignados al entrenador logueado
    @GetMapping("/clientes")
    public String verClientes(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario entrenador = usuarioService.findByUserName(username);
        
        // Filtramos para obtener solo los clientes que tienen este entrenador asignado
        List<Usuario> misClientes = usuarioService.findByEntrenadorId(entrenador.getId());
        
        model.addAttribute("usuarios", misClientes); 
        return "entrenador/mis_clientes"; 
    }

    // Endpoint: GET /entrenador/rutinas
    // Propósito: Mostrar la lista de rutinas de los clientes de este entrenador
    @GetMapping("/rutinas")
    public String verRutinas(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario entrenador = usuarioService.findByUserName(username);
        
        // Obtenemos solo las rutinas vinculadas a los clientes de este entrenador
        List<Rutina> rutinas = rutinaService.findByEntrenadorId(entrenador.getId());
        
        model.addAttribute("rutinas", rutinas);
        return "entrenador/ver_rutinas"; 
    }

    // Endpoint: GET /entrenador/rutinas/nueva
    // Propósito: Mostrar el formulario con TODOS los clientes para poder asignarles rutina
    @GetMapping("/rutinas/nueva")
    public String mostrarFormularioCreacionRutina(Model model) {
        model.addAttribute("rutinaForm", new RutinaFormDTO());
        
        // Cargamos todos los clientes para que el entrenador pueda elegir a cualquiera
        List<Usuario> todosLosClientes = usuarioService.findByRolNombre("CLIENTE");
        model.addAttribute("clientes", todosLosClientes);
        
        return "entrenador/form_rutina";
    }

    // Endpoint: POST /entrenador/rutinas
    // Propósito: Crear rutina y VINCULAR al cliente con el entrenador automáticamente
    @PostMapping("/rutinas")
    public String crearRutina(@Valid @ModelAttribute("rutinaForm") RutinaFormDTO rutinaForm,
                              BindingResult result,
                              Authentication auth,
                              Model model) {
        if (result.hasErrors()) {
            // Si hay error, recargamos todos los clientes para no perder el select
            model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
            return "entrenador/form_rutina";
        }

        try {
            // 1. Buscamos quién es el entrenador actual
            Usuario entrenadorActual = usuarioService.findByUserName(auth.getName());

            // 2. Buscamos al cliente seleccionado
            Usuario cliente = usuarioService.findByIdOrThrow(rutinaForm.getClienteId());

            // 3. VINCULACIÓN: Si el cliente no tiene entrenador, se lo asignamos al actual
            if (cliente.getEntrenador() == null) {
                cliente.setEntrenador(entrenadorActual);
                usuarioService.save(cliente);
            }

            // 4. Creamos la rutina
            rutinaService.crearRutinaDesdeDTO(rutinaForm);
            return "redirect:/entrenador/rutinas";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear la rutina: " + e.getMessage());
            model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
            return "entrenador/form_rutina";
        }
    }

    // Endpoint: GET /entrenador/rutinas/{id}/editar
    // Propósito: Mostrar el formulario de edición con los datos precargados
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

        // En edición, mostramos todos los clientes por si quiere reasignar la rutina
        model.addAttribute("rutinaForm", rutinaForm);
        model.addAttribute("rutinaId", id);
        model.addAttribute("clientes", usuarioService.findByRolNombre("CLIENTE"));
        return "entrenador/form_rutina";
    }

    // Endpoint: POST /entrenador/rutinas/{id}
    // Propósito: Actualizar rutina y asegurar que el cliente esté vinculado
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

        // 1. Actualizar la entidad Rutina
        Rutina rutinaExistente = rutinaService.findByIdOrThrow(id);
        rutinaExistente.setNombre(rutinaForm.getNombre());
        rutinaExistente.setDescripcion(rutinaForm.getDescripcion());
        rutinaExistente.setTipo(rutinaForm.getTipo());
        rutinaExistente.setNivelDificultad(rutinaForm.getNivelDificultad());
        rutinaExistente.setDuracionSemanas(rutinaForm.getDuracionSemanas());
        rutinaExistente.setActivo(rutinaForm.getActiva());

        // 2. Asegurar el vínculo con el cliente
        Usuario cliente = usuarioService.findByIdOrThrow(rutinaForm.getClienteId());
        Usuario entrenadorActual = usuarioService.findByUserName(auth.getName());

        if (cliente.getEntrenador() == null) {
            cliente.setEntrenador(entrenadorActual);
            usuarioService.save(cliente);
        }

        rutinaExistente.setCliente(cliente);
        rutinaService.save(rutinaExistente);
        
        return "redirect:/entrenador/rutinas";
    }

    // Endpoint: POST /entrenador/rutinas/{id}/eliminar
    // Propósito: Eliminar una rutina de forma segura
    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable Long id) {
        java.util.Optional<Rutina> rutinaOpt = rutinaService.findById(id);
        if (rutinaOpt.isPresent()) {
            rutinaService.deleteById(id);
        }
        return "redirect:/entrenador/rutinas";
    }
}