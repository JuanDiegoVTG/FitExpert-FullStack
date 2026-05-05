package com.proyecto.emilite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.PagoFormDTO;
import com.proyecto.emilite.repository.PagoRepository;
import com.proyecto.emilite.repository.ServicioRepository;
import com.proyecto.emilite.repository.UsuarioRepository; 

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; 

    @Autowired
    private ServicioRepository servicioRepository; 
    
    // --- MÉTODOS CRUD BÁSICOS ---

    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    public Pago findById(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    public Pago save(Pago pago) {
        return pagoRepository.save(pago);
    }

    public void deleteById(Long id) {
        pagoRepository.deleteById(id);
    }

    // MÉTODOS DE BÚSQUEDA ESPECÍFICOS

    public List<Pago> findByUsuarioId(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId);
    }

    // Busca un pago por su referencia de mercado pago. Útil para el retorno del checkout.
     
    public Pago findByReferencia(String referencia) {
        return pagoRepository.findByReferenciaPago(referencia).orElse(null);
    }

    // LÓGICA DE NEGOCIO Y PASARELA

    // Crea un pago desde el formulario del ADMIN.
    
    public void crearPagoDesdeDTO(PagoFormDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Servicio servicio = null;
        if (dto.getServicioId() != null) {
            servicio = servicioRepository.findById(dto.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        }

        Pago nuevoPago = new Pago();
        nuevoPago.setUsuario(usuario);
        nuevoPago.setServicio(servicio);
        nuevoPago.setMonto(dto.getMonto());
        nuevoPago.setMetodoPago(dto.getMetodoPago()); // Ej: "MERCADO_PAGO" o "EFECTIVO"
        nuevoPago.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        nuevoPago.setReferenciaPago(dto.getReferenciaPago());

        pagoRepository.save(nuevoPago);
    }

    //Método para actualizar el estado tras el retorno de Mercado Pago.
    public void actualizarEstado(String referencia, String estado) {
        pagoRepository.findByReferenciaPago(referencia).ifPresent(pago -> {
            pago.setEstado(estado);
            pagoRepository.save(pago);
        });
    }
}