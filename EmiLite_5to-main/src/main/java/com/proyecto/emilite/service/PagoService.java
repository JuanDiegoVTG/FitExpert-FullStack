package com.proyecto.emilite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.emilite.model.Notificacion;
import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.NotificacionRepository;
import com.proyecto.emilite.repository.PagoRepository;
import com.proyecto.emilite.repository.UsuarioRepository;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; 

    
    @Autowired
    private NotificacionRepository notificacionRepository; 

    @Autowired
    private ContratoService contratoService; // 🔑 La llave para activar tu IA

    // --- MÉTODOS CRUD ---

    public List<Pago> findAll() { return pagoRepository.findAll(); }

    public Pago findById(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public Pago save(Pago pago) { return pagoRepository.save(pago); }

    public void deleteById(Long id) { pagoRepository.deleteById(id); }

    public Pago findByReferencia(String referencia) {
        return pagoRepository.findByReferenciaPago(referencia).orElse(null);
    }

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Procesa la respuesta de Mercado Pago.
     * Si es exitoso: Actualiza el pago, notifica al entrenador y ACTIVA LA IA.
     */
    public void actualizarEstado(String referencia, String estado) {
        pagoRepository.findByReferenciaPago(referencia).ifPresent(pago -> {
            
            pago.setEstado(estado);
            pagoRepository.save(pago);

            // Si el pago es aprobado por Mercado Pago
            if ("approved".equalsIgnoreCase(estado) || "COMPLETADO".equalsIgnoreCase(estado)) {
                Usuario cliente = pago.getUsuario();
                
                if (cliente != null) {
                    // Buscamos al entrenador (el ID 2 que acordamos o el del servicio)
                    Usuario entrenador = (pago.getServicio() != null && pago.getServicio().getEntrenador() != null) 
                                         ? pago.getServicio().getEntrenador() 
                                         : usuarioRepository.findById(2L).orElse(null);

                    if (entrenador != null) {
                        // 🚀 AQUÍ SE ACTIVA TU IA: Creamos el contrato en BD
                        contratoService.crearVínculo(cliente, entrenador);
                        
                        // 🔔 NOTIFICACIÓN: Johan (Scrum Master) queda feliz
                        enviarNotiExito(cliente, entrenador, pago.getServicio());
                    }
                }
            }
        });
    }

    private void enviarNotiExito(Usuario cliente, Usuario entrenador, Servicio servicio) {
        Notificacion n = new Notificacion();
        n.setUsuario(entrenador);
        n.setRemitente(cliente);
        String nombreS = (servicio != null) ? servicio.getNombre() : "un plan";
        n.setMensaje("¡Venta realizada! " + cliente.getNombres() + " pagó " + nombreS + ". IA desbloqueada.");
        n.setLeida(false);
        notificacionRepository.save(n);
    }

    public List<Pago> findByUsuarioId(Long usuarioId) {
    return pagoRepository.findByUsuarioId(usuarioId);
    }
}