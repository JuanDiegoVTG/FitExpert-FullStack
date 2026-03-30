package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Pago;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.Promocion;
import com.proyecto.emilite.model.dto.PagoFormDTO;
import com.proyecto.emilite.repository.PagoRepository;
import com.proyecto.emilite.repository.UsuarioRepository; // Importa este repositorio
import com.proyecto.emilite.repository.ServicioRepository; // Importa este repositorio
import com.proyecto.emilite.repository.PromocionRepository; // Importa este repositorio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    // Inyectamos los otros repositorios necesarios
    @Autowired
    private UsuarioRepository usuarioRepository; // <-- Añade esta línea

    @Autowired
    private ServicioRepository servicioRepository; // <-- Añade esta línea

    @Autowired
    private PromocionRepository promocionRepository; // <-- Añade esta línea

    // Método para obtener todos los pagos
    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    // Método para encontrar un pago por ID
    // Este método SÍ debe usar Optional y orElseThrow porque findById de JpaRepository SIEMPRE devuelve Optional
    public Pago findById(Long id) {
        return pagoRepository.findById(id) // Este método devuelve Optional<Pago>
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id)); // <-- orElseThrow es válido aquí
    }

    // Método para guardar (crear o actualizar) un pago
    public Pago save(Pago pago) {
        return pagoRepository.save(pago);
    }

    // Conveniencia: obtener Pago o lanzar excepción si no existe
    public Pago findByIdOrThrow(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    // Método para eliminar un pago por ID
    public void deleteById(Long id) {
        pagoRepository.deleteById(id);
    }

    // Método para encontrar pagos por ID de usuario (usado por el cliente para ver sus pagos)
    public List<Pago> findByUsuarioId(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId);
    }

    // Método para crear un pago desde un DTO (usado por el cliente al pagar un servicio)
    public void crearPagoDesdeDTO(PagoFormDTO dto) {
        // Validar que el usuario exista
        // usuarioRepository.findById() devuelve Optional<Usuario>, por lo tanto, usamos orElseThrow
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()) // <-- findById devuelve Optional
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // Validar que el servicio exista (si se proporcionó)
        Servicio servicio = null;
        if (dto.getServicioId() != null) {
            // servicioRepository.findById() devuelve Optional<Servicio>, por lo tanto, usamos orElseThrow
            servicio = servicioRepository.findById(dto.getServicioId()) // <-- findById devuelve Optional
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + dto.getServicioId()));
        }

        // Validar que la promoción exista (si se proporcionó)
        Promocion promocion = null;
        if (dto.getPromocionId() != null) {
            // promocionRepository.findById() devuelve Optional<Promocion>, por lo tanto, usamos orElseThrow
            promocion = promocionRepository.findById(dto.getPromocionId()) // <-- findById devuelve Optional
                    .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + dto.getPromocionId()));
        }

        // Crear la entidad Pago
        Pago nuevoPago = new Pago();
        nuevoPago.setUsuario(usuario);
        nuevoPago.setServicio(servicio);
        nuevoPago.setPromocion(promocion);
        nuevoPago.setMonto(dto.getMonto());
        nuevoPago.setMetodoPago(dto.getMetodoPago());
        nuevoPago.setEstado(dto.getEstado());
        nuevoPago.setReferenciaPago(dto.getReferenciaPago());

        // Fecha de pago se asigna por defecto en la entidad Pago

        // Guardar el pago
        pagoRepository.save(nuevoPago); // <-- Asegúrate de que este método exista en PagoRepository
    }
}