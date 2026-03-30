package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Rutina;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.RutinaFormDTO;
import com.proyecto.emilite.repository.RutinaRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Rutina> findAll() {
        return rutinaRepository.findAll();
    }

    public Optional<Rutina> findById(Long id) {
        return rutinaRepository.findById(id);
    }

    // Conveniencia: obtener Rutina o lanzar excepción si no existe
    public Rutina findByIdOrThrow(Long id) {
        return rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con ID: " + id));
    }

    public Rutina save(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    public void deleteById(Long id) {
        rutinaRepository.deleteById(id);
    }

    // Método para encontrar rutinas por ID del cliente
    public List<Rutina> findByClienteId(Long clienteId) {
        return rutinaRepository.findByClienteId(clienteId);
    }

    // Método para crear una rutina desde un DTO (usado por el entrenador)
    public void crearRutinaDesdeDTO(RutinaFormDTO dto) {
        
        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId()));

        // Crear la entidad Rutina
        Rutina nuevaRutina = new Rutina();
        nuevaRutina.setNombre(dto.getNombre());
        nuevaRutina.setDescripcion(dto.getDescripcion());
        nuevaRutina.setNivelDificultad(dto.getNivelDificultad());
        nuevaRutina.setTipo(dto.getTipo());
        nuevaRutina.setDuracionSemanas(dto.getDuracionSemanas());
        nuevaRutina.setActivo(dto.getActiva());

        // Asignar el cliente a la rutina
        nuevaRutina.setCliente(cliente);

        // Guardar la rutina
        rutinaRepository.save(nuevaRutina);
    }
}