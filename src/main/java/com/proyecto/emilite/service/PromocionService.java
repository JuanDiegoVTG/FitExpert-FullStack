package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Promocion;
import com.proyecto.emilite.model.dto.PromocionFormDTO;
import com.proyecto.emilite.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    // Método para obtener todas las promociones
    public List<Promocion> findAll() {
        return promocionRepository.findAll();
    }

    // Método para obtener una promoción por ID (lanza excepción si no existe)
    public Promocion findById(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + id));
    }

    // Método para obtener una promoción por código
    public Optional<Promocion> findByCodigo(String codigo) {
        return promocionRepository.findByCodigo(codigo);
    }

    // Método para guardar (crear o actualizar) una promoción
    public Promocion save(Promocion promocion) {
        // Validar fechas antes de guardar 
        if (promocion.getFechaInicio().isAfter(promocion.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
      
        return promocionRepository.save(promocion);
    }

    // Conveniencia: obtener Promocion o lanzar excepción si no existe
    public Promocion findByIdOrThrow(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + id));
    }

    // Método para eliminar una promoción por ID
    public void deleteById(Long id) {
        promocionRepository.deleteById(id);
    }

    // Método para encontrar promociones activas
    public List<Promocion> findActivePromotions() {
        return promocionRepository.findByActivaTrue();
    }

    // Método para encontrar promociones activas y vigentes (dentro del rango de fechas)
    public List<Promocion> findActiveAndValidPromotions() {
        LocalDate now = LocalDate.now();
        
        return promocionRepository.findAll().stream() 
                .filter(p -> p.getActiva() && !p.getFechaInicio().isAfter(now) && !p.getFechaFin().isBefore(now))
                .toList();
       
    }

    public void crearPromocionDesdeDTO(PromocionFormDTO dto) {
        Promocion nuevaPromocion = new Promocion();
        nuevaPromocion.setCodigo(dto.getCodigo());
        nuevaPromocion.setDescripcion(dto.getDescripcion());
        nuevaPromocion.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        nuevaPromocion.setFechaInicio(dto.getFechaInicio());
        nuevaPromocion.setFechaFin(dto.getFechaFin());
        nuevaPromocion.setMaxUsos(dto.getMaxUsos());
        nuevaPromocion.setActiva(dto.getActiva());

        promocionRepository.save(nuevaPromocion);
    }

    // Comprueba si existe una promoción con el código dado
    public boolean existsByCodigo(String codigo) {
        return promocionRepository.existsByCodigo(codigo);
    }
} 