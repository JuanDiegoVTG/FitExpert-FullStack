package com.proyecto.emilite.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.PagoFormDTO;
import com.proyecto.emilite.service.PagoService;
import com.proyecto.emilite.service.ServicioService;
import com.proyecto.emilite.service.UsuarioService;
import com.proyecto.emilite.util.HtmlGenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioService servicioService;

   
    //  PARTE ADMIN

    @GetMapping
    public String listarPagos(Model model) {
        model.addAttribute("pagos", pagoService.findAll());
        return "pagos/lista_pagos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("pagoForm", new PagoFormDTO());
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());
        return "pagos/form_pago";
    }

    @PostMapping
    public String crearPago(@Valid @ModelAttribute("pagoForm") PagoFormDTO pagoForm,
                            BindingResult result,
                            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("servicios", servicioService.findAll());
            return "pagos/form_pago";
        }

        Pago nuevoPago = new Pago();

        
        Usuario usuario = usuarioService.findById(pagoForm.getUsuarioId());
        nuevoPago.setUsuario(usuario);

        
        if (pagoForm.getServicioId() != null) {
            Servicio servicio = servicioService.findById(pagoForm.getServicioId());
            nuevoPago.setServicio(servicio);
        }

        nuevoPago.setMonto(pagoForm.getMonto());
        nuevoPago.setMetodoPago(pagoForm.getMetodoPago());
        nuevoPago.setEstado(pagoForm.getEstado());
        nuevoPago.setReferenciaPago(pagoForm.getReferenciaPago());

        pagoService.save(nuevoPago);

        return "redirect:/pagos";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {

        Pago pago = pagoService.findById(id); 

        PagoFormDTO pagoForm = new PagoFormDTO();
        pagoForm.setUsuarioId(pago.getUsuario().getId());
        pagoForm.setServicioId(pago.getServicio() != null ? pago.getServicio().getId() : null);
        pagoForm.setMonto(pago.getMonto());
        pagoForm.setMetodoPago(pago.getMetodoPago());
        pagoForm.setEstado(pago.getEstado());
        pagoForm.setReferenciaPago(pago.getReferenciaPago());

        model.addAttribute("pagoForm", pagoForm);
        model.addAttribute("pagoId", id);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("servicios", servicioService.findAll());

        return "pagos/form_pago";
    }


        @PostMapping("/{id}")
    public String actualizarPago(@PathVariable Long id,
                                @Valid @ModelAttribute("pagoForm") PagoFormDTO pagoForm,
                                BindingResult result,
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pagoId", id);
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("servicios", servicioService.findAll());
            return "pagos/form_pago";
        }

        // PagoService devuelve Pago directo 
        Pago pagoExistente = pagoService.findById(id);

        
        Usuario usuario = usuarioService.findById(pagoForm.getUsuarioId());
        pagoExistente.setUsuario(usuario);

        if (pagoForm.getServicioId() != null) {
            Servicio servicio = servicioService.findById(pagoForm.getServicioId());
            pagoExistente.setServicio(servicio);
        } else {
            pagoExistente.setServicio(null);
        }

        pagoExistente.setMonto(pagoForm.getMonto());
        pagoExistente.setMetodoPago(pagoForm.getMetodoPago());
        pagoExistente.setEstado(pagoForm.getEstado());
        pagoExistente.setReferenciaPago(pagoForm.getReferenciaPago());

        pagoService.save(pagoExistente);

        return "redirect:/pagos";
    }


    @PostMapping("/{id}/eliminar")
    public String eliminarPago(@PathVariable Long id) {
        pagoService.deleteById(id);
        return "redirect:/pagos";
    }


    //  PARTE CLIENTE

    @GetMapping("/cliente")
    public String verPagosCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuarioLogueado = usuarioService.findByUserName(username);

        model.addAttribute("pagos", pagoService.findByUsuarioId(usuarioLogueado.getId()));

        return "cliente/mis_pagos";
    }


    //  GENERAR PDF

    @GetMapping("/cliente/pagar/{id}")
    public void pagarServicio(@PathVariable Long id,
                              HttpServletResponse response,
                              Model model) throws Exception {

        Servicio servicio = servicioService.findById(id); // este método devuelve directo
        model.addAttribute("servicio", servicio);

        String html = HtmlGenerator.generateHtml("comprobante", model);

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(pdfStream);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=comprobante_pago.pdf");
        response.getOutputStream().write(pdfStream.toByteArray());
        response.flushBuffer();
    }
}
