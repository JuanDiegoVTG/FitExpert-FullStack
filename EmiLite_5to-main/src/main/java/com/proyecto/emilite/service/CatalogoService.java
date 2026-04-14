package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerEntrenadoresActivos() {
        return usuarioRepository.findByRol_NombreAndActivo("ENTRENADOR", true);
    }
}

