package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    // --- GESTIÓN DE CLIENTES (opcional, si aplica) ---

    // Endpoint: GET /entrenador/clientes
    // Propósito: Mostrar la lista de clientes (solo para ENTRENADOR)
    @GetMapping("/clientes")
    public String verClientes(Model model) {
        // Aquí puedes implementar la lógica para obtener solo los clientes asignados al entrenador logueado
        // Por ahora, obtenemos todos los usuarios con rol CLIENTE
        List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");
        model.addAttribute("clientes", clientes);
        // La plantilla actual se llama `mis_clientes.html`, devolver esa vista
        return "entrenador/mis_clientes"; // Vista para mostrar la lista de clientes
    }

    // --- GESTIÓN DE RUTINAS (para ENTRENADOR) ---

    // Endpoint: GET /entrenador/rutinas
    // Propósito: Mostrar la lista de rutinas (pueden ser las del entrenador logueado o todas las asignadas a clientes)
    @GetMapping("/rutinas")
    public String verRutinas(Model model) {
        List<Rutina> rutinas = rutinaService.findAll(); // Puedes filtrar por entrenador si implementas esa lógica
        model.addAttribute("rutinas", rutinas);
        return "entrenador/ver_rutinas"; // Vista para mostrar la lista de rutinas (debes crearla)
    }

    // Endpoint: GET /entrenador/rutinas/nueva
    // Propósito: Mostrar el formulario para que el entrenador cree una nueva rutina
    @GetMapping("/rutinas/nueva")
    public String mostrarFormularioCreacionRutina(Model model) {
        model.addAttribute("rutinaForm", new RutinaFormDTO()); // Objeto vacío para el formulario
        // Cargar la lista de clientes para el select
        List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");
        model.addAttribute("clientes", clientes);
        return "entrenador/form_rutina"; // Vista para el formulario de creación (debes crearla)
    }

    // Endpoint: POST /entrenador/rutinas
    // Propósito: Procesar el formulario de creación de una nueva rutina
    @PostMapping("/rutinas")
    public String crearRutina(@Valid @ModelAttribute("rutinaForm") RutinaFormDTO rutinaForm,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");
            model.addAttribute("clientes", clientes);
            return "entrenador/form_rutina";
        }

        try {
            // Llamar al servicio para crear la rutina desde el DTO
            rutinaService.crearRutinaDesdeDTO(rutinaForm);
            // Si la creación es exitosa, redirige a la lista de rutinas
            return "redirect:/entrenador/rutinas";
        } catch (Exception e) {
            // Manejo de errores inesperados
            model.addAttribute("error", "Error al crear la rutina: " + e.getMessage());
            // Recargar la lista de clientes para el select
            List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");
            model.addAttribute("clientes", clientes);
            return "entrenador/form_rutina";
        }
    }

    // --- NUEVO: Endpoint para mostrar el formulario de edición de una rutina ---
    // Endpoint: GET /entrenador/rutinas/{id}/editar
    // Propósito: Mostrar el formulario para que el entrenador edite una rutina existente
    @GetMapping("/rutinas/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        // Buscar la rutina por ID
        // Asegúrate de que findById en RutinaService devuelva Rutina o null, no Optional
        Rutina rutina = rutinaService.findByIdOrThrow(id);

        // Convertir la entidad Rutina a DTO para el formulario
        RutinaFormDTO rutinaForm = new RutinaFormDTO();
        rutinaForm.setNombre(rutina.getNombre());
        rutinaForm.setDescripcion(rutina.getDescripcion());
        rutinaForm.setTipo(rutina.getTipo());
        rutinaForm.setNivelDificultad(rutina.getNivelDificultad());
        rutinaForm.setDuracionSemanas(rutina.getDuracionSemanas());
        rutinaForm.setActiva(rutina.getActivo()); // Asumiendo que getActivo() devuelve Boolean
        rutinaForm.setClienteId(rutina.getCliente().getId()); // Pasar solo el ID del cliente

        // Cargar la lista de clientes para el select
        List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");

        model.addAttribute("rutinaForm", rutinaForm);
        model.addAttribute("rutinaId", id); // Pasar el ID para el POST de edición
        model.addAttribute("clientes", clientes); // Pasar la lista de clientes
        return "entrenador/form_rutina"; // Vista para el formulario de edición (misma que para creación, pero con datos precargados)
    }
    // --- FIN NUEVO MÉTODO ---

    // --- NUEVO: Endpoint para procesar la edición de una rutina ---
    // Endpoint: POST /entrenador/rutinas/{id}
    // Propósito: Procesar el formulario de edición de una rutina existente
    @PostMapping("/rutinas/{id}")
    public String actualizarRutina(@PathVariable Long id,
                                   @Valid @ModelAttribute("rutinaForm") RutinaFormDTO rutinaForm,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            model.addAttribute("rutinaId", id);
            // Recargar la lista de clientes para el select
            List<Usuario> clientes = usuarioService.findByRolNombre("CLIENTE");
            model.addAttribute("clientes", clientes);
            return "entrenador/form_rutina";
        }

        // Buscar la rutina existente por ID
        // Asegúrate de que findById en RutinaService devuelva Rutina o null, no Optional
        Rutina rutinaExistente = rutinaService.findByIdOrThrow(id);

        // Actualizar la entidad con los datos del DTO
        rutinaExistente.setNombre(rutinaForm.getNombre());
        rutinaExistente.setDescripcion(rutinaForm.getDescripcion());
        rutinaExistente.setTipo(rutinaForm.getTipo());
        rutinaExistente.setNivelDificultad(rutinaForm.getNivelDificultad());
        rutinaExistente.setDuracionSemanas(rutinaForm.getDuracionSemanas());
        rutinaExistente.setActivo(rutinaForm.getActiva());

        // Actualizar cliente
        Usuario cliente = usuarioService.findByIdOrThrow(rutinaForm.getClienteId());
        rutinaExistente.setCliente(cliente);

        // Guardar la rutina actualizada
        rutinaService.save(rutinaExistente);

        // Redirigir a la lista de rutinas después de actualizar
        return "redirect:/entrenador/rutinas";
    }
    // --- FIN NUEVO MÉTODO ---

    // --- NUEVO: Endpoint para eliminar una rutina ---
    // Endpoint: POST /entrenador/rutinas/{id}/eliminar
    // Propósito: Eliminar una rutina existente
    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable Long id) {
        // Verificar si la rutina existe (opcional, pero buena práctica)
        java.util.Optional<Rutina> rutinaOpt = rutinaService.findById(id);
        if (rutinaOpt.isEmpty()) {
            // Si no existe, simplemente redirigimos sin lanzar excepción
            return "redirect:/entrenador/rutinas";
        }
        rutinaService.deleteById(id);
        // Redirigir a la lista de rutinas después de eliminar
        return "redirect:/entrenador/rutinas";
    }
    // --- FIN NUEVO MÉTODO ---
}