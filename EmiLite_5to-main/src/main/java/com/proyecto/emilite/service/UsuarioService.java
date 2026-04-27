package com.proyecto.emilite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.emilite.dto.UsuarioRegistroDTO;
import com.proyecto.emilite.model.Perfil;
import com.proyecto.emilite.model.Rol;
import com.proyecto.emilite.model.Usuario;
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
    

    public static final String ROL_ENTRENADOR = "ROLE_ENTRENADOR";
    public static final String ROL_CLIENTE = "ROLE_CLIENTE";

    // --- 1. GESTIÓN Y REGISTRO (Resuelve AdminUsuarioController) ---

    @Transactional
    public void registrar(UsuarioRegistroDTO dto) {
        // 1. VALIDACIONES DE CAMPOS OBLIGATORIOS (Blindaje total)
        validarCamposRegistro(dto);

        // 2. VALIDACIONES DE UNICIDAD (Base de Datos)
        if (usuarioRepository.existsByUserName(dto.getUserName())) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUserName() + "' ya está en uso.");
        }
        
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo '" + dto.getEmail() + "' ya está registrado.");
        }

        // 3. CREACIÓN DEL USUARIO
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

        // Asignación de Rol
        Long idRol = (dto.getRolId() != null) ? dto.getRolId() : 3L; 
        usuario.setValidado(idRol != 2); 

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Error: El rol seleccionado no es válido."));
        usuario.setRol(rol);

        // 4. CREACIÓN DEL PERFIL CON ATRIBUTO SEXO (Lógica Pro)
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        perfil.setSexo(dto.getSexo()); 
        perfil.setNombreCompleto(dto.getNombres() + " " + dto.getApellidos());
        
        usuario.setPerfil(perfil);

        // 5. GUARDADO
        usuarioRepository.save(usuario);
    }

    /**
     * Método auxiliar para validar que nada llegue vacío
     */
    private void validarCamposRegistro(UsuarioRegistroDTO dto) {
        if (dto.getUserName() == null || dto.getUserName().trim().isEmpty()) 
            throw new RuntimeException("El nombre de usuario es obligatorio.");
        
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) 
            throw new RuntimeException("La contraseña no puede estar vacía.");
        
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) 
            throw new RuntimeException("El correo electrónico es obligatorio.");
        
        if (dto.getNombres() == null || dto.getNombres().trim().isEmpty()) 
            throw new RuntimeException("El nombre es obligatorio.");
        
        if (dto.getApellidos() == null || dto.getApellidos().trim().isEmpty()) 
            throw new RuntimeException("El apellido es obligatorio.");
        
        if (dto.getFechaNacimiento() == null) 
            throw new RuntimeException("La fecha de nacimiento es obligatoria.");
        
        if (dto.getSexo() == null || dto.getSexo().trim().isEmpty()) 
            throw new RuntimeException("El sexo biológico es obligatorio para cálculos de salud.");
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
     * Ahora el servicio decide qué rol buscar, 
     * pasando el nombre correcto de la base de datos: 'ROLE_ENTRENADOR'
     */
    public List<Usuario> buscarEntrenadores(String keyword) {
        // Pasamos el parámetro 'ROLE_ENTRENADOR' al nuevo método del repository
        return usuarioRepository.buscarPorRolYKeyword("ROLE_ENTRENADOR", keyword);
    }

    public List<Usuario> listarEntrenadoresActivos() {
        return usuarioRepository.listarPorRolActivo("ROLE_ENTRENADOR");
    }

    // --- ¡Y AHORA TAMBIÉN PUEDES HACER ESTO PARA CLIENTES! ---
    public List<Usuario> buscarClientes(String keyword) {
        // Basta con cambiar el parámetro a 'ROLE_CLIENTE'
        return usuarioRepository.buscarPorRolYKeyword("ROLE_CLIENTE", keyword);
    }

    public List<Usuario> buscarPorRolYKeyword(String nombreRol, String keyword) {
        return usuarioRepository.buscarPorRolYKeyword(nombreRol, keyword);
    }

    public List<Usuario> listarPorRolActivo(String nombreRol) {
        return usuarioRepository.listarPorRolActivo(nombreRol);
    }

    // --- 4. VALIDACIONES Y PERSISTENCIA (Resuelve UsuarioController) ---

    public boolean existsByUserName(String username) {
        return usuarioRepository.existsByUserName(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    // Este es el que necesitamos para el Catálogo y la activación
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElse(null); 
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

    public void registrarConCv(UsuarioRegistroDTO dto, String nombrePdf) {
        // 1. Creamos la instancia de la Entidad (El modelo de la BD)
        Usuario usuario = new Usuario();

        // 2. Mapeamos uno por uno los campos del DTO al Usuario
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setUserName(dto.getUserName());
        usuario.setEmail(dto.getEmail());
        // 3. ¡IMPORTANTE! Encriptar la contraseña antes de guardar
        usuario.setFechaNacimiento(dto.getFechaNacimiento()); 
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRutaHojaVida(nombrePdf);
        
        

        // 5. Asignar el Rol
        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        usuario.setRol(rol);
        
        // Lógica de validación
        if (dto.getRolId() == 2) {
            usuario.setValidado(false);
            usuario.setActivo(false); // Inactivo hasta que el admin apruebe
        } else {
            usuario.setValidado(true);
            usuario.setActivo(true);
        }

        usuarioRepository.save(usuario);
    }
}