package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.ClientePerfilDTO;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PagoService;
import com.proyecto.emilite.service.PythonService;
import com.proyecto.emilite.service.ServicioService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;


@Controller
public class ClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;
     
    @Autowired
    private ServicioService servicioService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PythonService pythonService;

    @Autowired
    private RutinaRepository rutinaRepository;


    // Endpoint: GET /cliente/servicios
    // Propósito: Mostrar la lista de servicios disponibles para el cliente
    @GetMapping("/cliente/servicios")
    public String verServiciosCliente(Model model) {
        // Obtener todos los servicios activos (o todos, dependiendo de tu lógica)
        List<Servicio> servicios = servicioService.findByActivo(true); // Asumiendo que tienes este método en ServicioService

        // Añadir la lista de servicios al modelo para que la vista pueda mostrarla
        model.addAttribute("servicios", servicios);

        // Devolver la vista específica para mostrar los servicios al cliente
        return "cliente/ver_servicios"; // Vista que crearemos a continuación
    }

    // Endpoint: GET /cliente/rutinas
    // Mostrar las rutinas del cliente logueado
    @GetMapping("/cliente/rutinas")
    public String verMisRutinas(Authentication auth, Model model) {
        // 1. Saber quién es el usuario logueado
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar todas LAS RUTINAS de ese usuario específico
        List<Rutina> misRutinas = rutinaRepository.findByClienteId(usuario.getId());

        // 3. Pasar la lista completa al HTML
        model.addAttribute("rutinas", misRutinas);
        return "cliente/ver_rutinas";
    }

     @GetMapping("/cliente/perfil/editar")
    public String mostrarFormularioEdicionPerfil(Model model) {
        // Obtener el nombre de usuario del usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener la entidad Usuario desde la base de datos
        Usuario usuarioLogueado = usuarioService.findByUserName(username);

        // Convertir la entidad Usuario a DTO para el formulario
        ClientePerfilDTO perfilDTO = new ClientePerfilDTO();
        perfilDTO.setEmail(usuarioLogueado.getEmail());
        perfilDTO.setTelefono(usuarioLogueado.getTelefono());
        perfilDTO.setDireccion(usuarioLogueado.getDireccion());
        perfilDTO.setNombres(usuarioLogueado.getNombres());
        perfilDTO.setApellidos(usuarioLogueado.getApellidos());

        model.addAttribute("perfilForm", perfilDTO);
        return "cliente/editar_perfil"; // Vista para el formulario de edición de perfil
    }

    // Endpoint: POST /cliente/perfil/editar
    //Procesar el formulario de edición de perfil del cliente logueado
    @PostMapping("/cliente/perfil/editar")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilForm") ClientePerfilDTO perfilForm,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            return "cliente/editar_perfil";
        }

        // Obtener el nombre de usuario del usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener la entidad Usuario desde la base de datos
        Usuario usuarioLogueado = usuarioService.findByUserName(username);

        // Actualizar solo los campos permitidos en la entidad Usuario
        usuarioLogueado.setEmail(perfilForm.getEmail());
        usuarioLogueado.setTelefono(perfilForm.getTelefono());
        usuarioLogueado.setDireccion(perfilForm.getDireccion());
        usuarioLogueado.setNombres(perfilForm.getNombres());
        usuarioLogueado.setApellidos(perfilForm.getApellidos());
        

        // Guardar el usuario actualizado
        usuarioService.save(usuarioLogueado);

        
        return "redirect:/cliente/perfil/editar?success";
    }

    // Otro endpoint para ver el perfil (
    @GetMapping("/cliente/perfil")
    public String verPerfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuarioLogueado = usuarioService.findByUserName(username);
        model.addAttribute("usuario", usuarioLogueado);
        return "cliente/ver_perfil"; // Vista de solo lectura
    }

     @GetMapping("/cliente/pagos") // <-- Mapeo correcto
    public String verPagosCliente(Model model) {
        // Obtener el nombre de usuario del usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener la entidad Usuario desde la base de datos
        Usuario usuarioLogueado = usuarioService.findByUserName(username);

        // Obtener los pagos asociados a este usuario
        List<Pago> pagosDelCliente = pagoService.findByUsuarioId(usuarioLogueado.getId());

        // Añadir la lista de pagos al modelo para que la vista pueda mostrarla
        model.addAttribute("pagos", pagosDelCliente);

        // Devolver la vista específica para los pagos del cliente
        return "cliente/mis_pagos";
    }    

    //CREAR RUTINA

    @GetMapping("/cliente/rutina") // Cuando el usuario entra a la pestaña "Mi Rutina"
    public String verRutina(Model model, Authentication auth) {

        // Le pedimos al servicio de Python que nos traiga la rutina
        String rutinaJson = pythonService.generarRutinaDesdePerfil(auth);

        // Esto sirve para que la página HTML pueda leer los datos que trajo Python
        model.addAttribute("rutina", rutinaJson);

        // Le decimos a Spring que abra la vista (el archivo HTML)
        return "cliente/rutina";
    }

    @GetMapping("/cliente/preparar-rutina")
    public String mostrarFormularioIA() {
    return "cliente/preparar_rutina";
    }
    
}