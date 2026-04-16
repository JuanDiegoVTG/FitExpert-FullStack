package com.proyecto.emilite.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.model.dto.UsuarioRegistroDTO; // Ajustado a tu estructura
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

    // --- 1. GESTIÓN Y REGISTRO (Resuelve AdminUsuarioController) ---

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

        // ID 2 = ENTRENADOR | ID 3 = CLIENTE
        Long idRol = (dto.getRolId() != null) ? dto.getRolId() : 3L; 
        
        // Entrenadores (2) quedan inactivos hasta que el admin los valide
        usuario.setValidado(idRol != 2); 

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    // Requerido por AdminUsuarioController.java
    public void crearUsuarioDesdeDTO(UsuarioRegistroDTO dto) {
        registrar(dto);
    }

    // --- 2. BÚSQUEDAS GENERALES (Resuelve Dashboard y Reportes) ---

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Requerido por DashboardController.java
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID no encontrado: " + id));
    }

    // Requerido por EntrenadorController.java
    public Usuario findByIdOrThrow(Long id) {
        return findById(id);
    }

    public Usuario findByUserName(String username) {
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    // Requerido por EntrenadorController.java (Lista de alumnos)
    public List<Usuario> findByEntrenadorId(Long entrenadorId) {
        return usuarioRepository.findByEntrenadorId(entrenadorId);
    }

    // Requerido por ReporteController y Admin para los filtros de tablas
    public List<Usuario> findByFilters(String rolNombre, Boolean activo) {
        if (rolNombre == null && activo == null) return findAll();
        if (rolNombre != null && activo != null) return usuarioRepository.findByRolNombreAndActivo(rolNombre, activo);
        if (rolNombre != null) return usuarioRepository.findByRolNombre(rolNombre);
        return usuarioRepository.findByActivo(activo);
    }

    // --- 3. LÓGICA DEL CATÁLOGO (Optimizado para Mercado Pago) ---

    public List<Usuario> findByRolNombre(String rolNombre) {
        return usuarioRepository.findByRolNombre(rolNombre);
    }

    /**
     * Usa la @Query personalizada del Repository para buscar entrenadores
     * Es mucho más rápido que usar streams de Java.
     */
    public List<Usuario> buscarPorNombreOEspecialidad(String keyword) {
        return usuarioRepository.buscarEntrenadores(keyword);
    }

    public List<Usuario> buscarPorCalificacion(Integer calificacion) {
        // Por ahora devuelve todos los entrenadores activos
        return usuarioRepository.listarEntrenadoresActivos();
    }

    // --- 4. VALIDACIONES Y PERSISTENCIA (Resuelve UsuarioController) ---

    public boolean existsByUserName(String username) {
        return usuarioRepository.existsByUserName(username);
    }

    // Requerido por UsuarioController.java
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Obtiene la lista de clientes asignados a un entrenador específico.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerClientesDeEntrenador(Long entrenadorId) {
        return usuarioRepository.findByEntrenadorId(entrenadorId);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * Lanza una excepción si no se encuentra.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

}