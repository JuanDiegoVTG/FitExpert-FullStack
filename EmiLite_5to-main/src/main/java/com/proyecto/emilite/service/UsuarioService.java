package com.proyecto.emilite.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.repository.RolRepository;
import com.proyecto.emilite.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolRepository rolRepository;


    // --- MÉTODO DE REGISTRO (EL QUE USA EL AUTH CONTROLLER) ---
    @Transactional
    public void registrar(UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsByUserName(dto.getUserName())) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUserName() + "' ya está en uso.");
        }

        // 2. Validar Email único
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Este correo ya está registrado");
        }

        // 3. Validar Edad mínima (Opcional pero pro para el SENA)
        if (Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 14) {
            throw new RuntimeException("Debes tener al menos 14 años para registrarte");
        }

        Usuario usuario = new Usuario();
        usuario.setUserName(dto.getUserName());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmail());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setActivo(true);

        Long idRol = (dto.getRolId() != null) ? dto.getRolId() : 2L;
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);

        usuarioRepository.save(usuario);
    }
    
    // Este método lo buscan tus controladores de Admin y Usuario
    public void crearUsuarioDesdeDTO(UsuarioRegistroDTO dto) {
        registrar(dto); // Simplemente llamamos al método nuevo para no repetir código
    }

    // Este lo busca el ReporteController y AdminController para los filtros
    public List<Usuario> findByFilters(String rolNombre, Boolean activo) {
        if (rolNombre == null && activo == null) return findAll();
        if (rolNombre != null && activo != null) return usuarioRepository.findByRolNombreAndActivo(rolNombre, activo);
        if (rolNombre != null) return usuarioRepository.findByRolNombre(rolNombre);
        return usuarioRepository.findByActivo(activo);
    }

    // --- BÚSQUEDAS ---

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID no encontrado: " + id));
    }

    public Usuario findByUserName(String username) {
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Username no encontrado: " + username));
    }

    public Usuario findByIdOrThrow(Long id) {
        return findById(id);
    }

    public List<Usuario> findByRolNombre(String rolNombre) {
        return usuarioRepository.findByRolNombre(rolNombre);
    }

    public List<Usuario> findByEntrenadorId(Long entrenadorId) {
        return usuarioRepository.findByEntrenadorId(entrenadorId);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // --- VALIDACIONES Y GUARDADO ---

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean existsByUserName(String username) {
        return usuarioRepository.existsByUserName(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public List<Rol> findAllRoles() {
        return rolRepository.findAll();
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}