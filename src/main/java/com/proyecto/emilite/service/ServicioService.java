package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Servicio;
import com.proyecto.emilite.model.dto.ServicioFormDTO;
import com.proyecto.emilite.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    public List<Servicio> findAll() {
        return servicioRepository.findAll();
    }

    // Devuelve Servicio o lanza una excepción si no se encuentra
    public Servicio findById(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
    }

    public Servicio save(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    // Conveniencia: obtener Servicio o lanzar excepción si no existe
    public Servicio findByIdOrThrow(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
    }

    public void deleteById(Long id) {
        servicioRepository.deleteById(id);
    }

    // Método para encontrar servicios activos
    public List<Servicio> findActiveServices() {
        return servicioRepository.findAll()
                .stream()
                .filter(Servicio::getActivo) // <-- CORRECTO: Llama a getActivo() en cada objeto Servicio
                .toList();
    }

    // Método para encontrar servicios por estado activo/inactivo
    public List<Servicio> findByActivo(Boolean activo) {
        return servicioRepository.findByActivo(activo); // Asegúrate de que ServicioRepository tenga este método
    }

    // Método para crear un servicio desde un DTO
    public void crearServicioDesdeDTO(ServicioFormDTO dto) {
        Servicio nuevoServicio = new Servicio();
        nuevoServicio.setNombre(dto.getNombre());
        nuevoServicio.setDescripcion(dto.getDescripcion());
        nuevoServicio.setDuracionMinutos(dto.getDuracionMinutos());
        nuevoServicio.setPrecio(dto.getPrecio());
        // Asegúrate de que el DTO tenga getActivo() y que el valor sea Boolean
        nuevoServicio.setActivo(dto.getActivo()); // <-- Asumiendo que ServicioFormDTO tiene getActivo()

        servicioRepository.save(nuevoServicio);
    }
}