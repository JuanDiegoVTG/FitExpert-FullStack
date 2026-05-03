package com.proyecto.emilite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.emilite.dto.ClientePerfilDTO;
import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import com.proyecto.emilite.service.PagoService;
import com.proyecto.emilite.service.ServicioService;
import com.proyecto.emilite.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/cliente") // Agrupamos todas las rutas bajo /cliente
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
    private RutinaRepository rutinaRepository;


    // --- PERFIL ---

    @GetMapping("/perfil/editar")
    public String mostrarFormularioEdicionPerfil(Authentication auth, Model model) {
        Usuario usuarioLogueado = usuarioService.findByUserName(auth.getName());
        Perfil perfilEntidad = usuarioLogueado.getPerfil();

        ClientePerfilDTO dto = new ClientePerfilDTO();
        // Pasamos los datos de la entidad al DTO para que el form aparezca lleno
        dto.setId(perfilEntidad.getId());
        dto.setNombreCompleto(usuarioLogueado.getNombres() + " " + usuarioLogueado.getApellidos());
        dto.setEdad(perfilEntidad.getEdad());
        dto.setSexo(perfilEntidad.getSexo());
        dto.setPeso(perfilEntidad.getPeso());
        dto.setAltura(perfilEntidad.getAltura());
        dto.setNivelActividad(perfilEntidad.getNivelActividad());
        dto.setCuello(perfilEntidad.getCuello());
        dto.setCintura(perfilEntidad.getCintura());
        dto.setCadera(perfilEntidad.getCadera());
        dto.setObjetivo(perfilEntidad.getObjetivo());

        

        // El nombre "perfil" debe ser igual al th:object="${perfil}"
        model.addAttribute("perfil", dto); 
        return "cliente/editar_perfil";
    }

    @PostMapping("/perfil/editar")
    public String actualizarPerfil(@Valid @ModelAttribute("perfil") ClientePerfilDTO perfilForm,
                                BindingResult result,
                                Authentication auth,
                                RedirectAttributes flash) {

        if (perfilForm.getTelefono() != null) {
            perfilForm.setTelefono(perfilForm.getTelefono().replace(" ", ""));
        }
        
        // Si hay errores, nos quedamos para ver qué pasó
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println); // 🚩 Esto te dirá la verdad en la consola
            return "cliente/editar_perfil";
        }

        try {
            Usuario usuarioActual = usuarioService.findByUserName(auth.getName());
            Perfil perfilEntidad = usuarioActual.getPerfil();

            // Sincronizamos datos
            usuarioActual.setEmail(perfilForm.getEmail());
            usuarioActual.setTelefono(perfilForm.getTelefono());
            
            perfilEntidad.setPeso(perfilForm.getPeso());
            perfilEntidad.setAltura(perfilForm.getAltura());
            perfilEntidad.setEdad(perfilForm.getEdad());
            perfilEntidad.setSexo(perfilForm.getSexo());
            perfilEntidad.setNivelActividad(perfilForm.getNivelActividad());
            perfilEntidad.setObjetivo(perfilForm.getObjetivo());

            // Guardamos
            usuarioService.save(usuarioActual);

            flash.addFlashAttribute("success", "¡Perfil actualizado! 🚀");
            return "redirect:/dashboard"; // Asegúrate que esta URL sea la correcta
            
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/cliente/perfil/editar";
        }
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        Usuario usuarioLogueado = usuarioService.findByUserName(auth.getName());
        model.addAttribute("usuario", usuarioLogueado);
        return "cliente/ver_perfil";
    }

    // --- SERVICIOS ---

    @GetMapping("/servicios")
    public String verServiciosCliente(Model model) {
        List<Servicio> servicios = servicioService.findByActivo(true);
        model.addAttribute("servicios", servicios);
        return "cliente/ver_servicios";
    }

    // --- RUTINAS ---

    @GetMapping("/rutinas")
    public String verMisRutinas(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Rutina> misRutinas = rutinaRepository.findByClienteId(usuario.getId());
        model.addAttribute("rutinas", misRutinas);
        return "cliente/ver_rutinas";
    }

    // --- PAGOS ---

    @GetMapping("/pagos")
    public String verPagosCliente(Authentication auth, Model model) {
        Usuario usuarioLogueado = usuarioService.findByUserName(auth.getName());
        List<Pago> pagosDelCliente = pagoService.findByUsuarioId(usuarioLogueado.getId());
        model.addAttribute("pagos", pagosDelCliente);
        return "cliente/mis_pagos";
    }
}