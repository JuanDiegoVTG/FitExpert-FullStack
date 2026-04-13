/*package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> buscar(String keyword, Double calificacion) {

        // 🔹 Caso 1: búsqueda con texto
        if (keyword != null && !keyword.trim().isEmpty()) {

            List<Usuario> resultados = usuarioRepository.buscarEntrenadores(keyword);

            // 🔹 Si además hay filtro por calificación
            if (calificacion != null) {
                return resultados.stream()
                        .filter(u -> u.getCvScore() != null && u.getCvScore() >= calificacion)
                        .toList();
            }

            return resultados;
        }

        // 🔹 Caso 2: solo filtro por calificación (sin keyword)
        if (calificacion != null) {
            return usuarioRepository.findByRolNombreAndActivo("ENTRENADOR", true)
                    .stream()
                    .filter(u -> u.getCvScore() != null && u.getCvScore() >= calificacion)
                    .toList();
        }

        // 🔹 Caso 3: sin filtros → todos los entrenadores activos
        return usuarioRepository.findByRolNombreAndActivo("ENTRENADOR", true);
    }
}

*/