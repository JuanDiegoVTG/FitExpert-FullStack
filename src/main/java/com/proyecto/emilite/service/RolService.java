package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    public Optional<Rol> findById(Long id) {
        return rolRepository.findById(id);
    }

    // Conveniencia: obtener Rol o lanzar excepción si no existe
    public Rol findByIdOrThrow(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
    }

    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }

    public void deleteById(Long id) {
        rolRepository.deleteById(id);
    }

    // Método para encontrar roles por nombre (devuelve una lista, útil si hay más de uno con el mismo nombre)
    public List<Rol> findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre); // Asegúrate de que RolRepository tenga este método
    }

    // Método para encontrar un rol por nombre (devuelve uno solo, útil si el nombre es único)
    public Optional<Rol> findOneByNombre(String nombre) {
        List<Rol> roles = findByNombre(nombre);
        if (roles.isEmpty()) {
            return Optional.empty();
        }
        // Si asumes que el nombre es único, devuelve el primero
        return Optional.of(roles.get(0));
    }
}