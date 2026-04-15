package com.proyecto.emilite.service;

import java.util.List;
import java.util.stream.Collectors;

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

    // --- 1. GESTIÓN Y REGISTRO ---

    @Transactional
    public void registrar(UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsByUserName(dto.getUserName())) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }
        
        Usuario usuario = new Usuario();
        usuario.setUserName(dto.getUserName());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmail());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setActivo(true);
        usuario.setEnabled(true);
        usuario.setEsPremuim(false);

        Long idRol = (dto.getRolId() != null) ? dto.getRolId() : 3L; 
        usuario.setValidado(idRol != 2); 

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    // Requerido por AdminUsuarioController
    public void crearUsuarioDesdeDTO(UsuarioRegistroDTO dto) {
        registrar(dto);
    }

    // --- 2. BÚSQUEDAS Y FILTROS (Corrige ReporteController y Admin) ---

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Requerido por DashboardController
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // 🔥 EL QUE TE FALTABA: Requerido por ReporteController y Admin
    public List<Usuario> findByFilters(String rolNombre, Boolean activo) {
        if (rolNombre == null && activo == null) return findAll();
        if (rolNombre != null && activo != null) return usuarioRepository.findByRolNombreAndActivo(rolNombre, activo);
        if (rolNombre != null) return usuarioRepository.findByRolNombre(rolNombre);
        return usuarioRepository.findByActivo(activo);
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID no encontrado: " + id));
    }

    // Requerido por EntrenadorController
    public Usuario findByIdOrThrow(Long id) {
        return findById(id);
    }

    public Usuario findByUserName(String username) {
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    // Requerido por EntrenadorController
    public List<Usuario> findByEntrenadorId(Long entrenadorId) {
        return usuarioRepository.findByEntrenadorId(entrenadorId);
    }

    // --- 3. LÓGICA DEL CATÁLOGO ---

    public List<Usuario> findByRolNombre(String rolNombre) {
        return usuarioRepository.findByRolNombre(rolNombre);
    }

    public List<Usuario> buscarPorNombreOEspecialidad(String keyword) {
        return usuarioRepository.findByNombresContainingIgnoreCaseOrDescripcionContainingIgnoreCase(keyword, keyword)
                .stream()
                .filter(u -> u.getRol().getNombre().equals("ENTRENADOR"))
                .collect(Collectors.toList());
    }

    public List<Usuario> buscarPorCalificacion(Integer calificacion) {
        return findByRolNombre("ENTRENADOR"); 
    }

    // --- 4. VALIDACIONES (Corrige UsuarioController) ---

    public boolean existsByUserName(String username) {
        return usuarioRepository.existsByUserName(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}