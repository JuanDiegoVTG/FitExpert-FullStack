package com.proyecto.emilite.service;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolService rolService;


    // CRUD BÁSICO 

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // devuelve Usuario
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // Devuelve Usuario o lanza excepción si no se encuentra
    public Usuario findByUserName(String username) {
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));
    }

    // Comprueba si existe un usuario por username
    public boolean existsByUserName(String username) {
        return usuarioRepository.existsByUserName(username);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Conveniencia: obtener Usuario o lanzar excepción si no existe
    public Usuario findByIdOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }


    //CREAR DESDE DTO

    public void crearUsuarioDesdeDTO(UsuarioRegistroDTO dto) {

        if (usuarioRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUserName() + "' ya está en uso.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUserName(dto.getUserName());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setNombres(dto.getNombres());
        nuevoUsuario.setApellidos(dto.getApellidos());
        nuevoUsuario.setTelefono(dto.getTelefono());
        nuevoUsuario.setDireccion(dto.getDireccion());
        nuevoUsuario.setFechaNacimiento(dto.getFechaNacimiento());

        Rol rol = rolService.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + dto.getRolId()));
        nuevoUsuario.setRol(rol);

        usuarioRepository.save(nuevoUsuario);
    }


    

    public List<Rol> findAllRoles() {
        return rolService.findAll();
    }

    public List<Usuario> findByFilters(String rolNombre, Boolean activo) {

        if (rolNombre == null && activo == null)
            return findAll();

        if (rolNombre != null && activo != null)
            return usuarioRepository.findByRolNombreAndActivo(rolNombre, activo);

        if (rolNombre != null)
            return usuarioRepository.findByRolNombre(rolNombre);

        return usuarioRepository.findByActivo(activo);
    }

    public List<Usuario> findByRolNombre(String rolNombre) {
        return findByFilters(rolNombre, null);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Comprueba si existe un usuario por email
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}
