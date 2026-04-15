package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // --- 1. SEGURIDAD Y ACCESO ---

    Optional<Usuario> findByUserName(String userName);
    
    boolean existsByUserName(String userName);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.userName = :userName")
    Optional<Usuario> findByUserNameWithRol(@Param("userName") String userName);


    // --- 2. BÚSQUEDAS POR ROL Y ESTADO (Admin y Reportes) ---

    List<Usuario> findByRolNombre(String rolNombre);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByRolNombreAndActivo(String rolNombre, Boolean activo);


    // --- 3. LÓGICA DE VÍNCULOS (IA y Contratos) ---

    /**
     * Busca los clientes asignados a un entrenador.
     */
    List<Usuario> findByEntrenadorId(Long entrenadorId);


    // --- 4. CATÁLOGO DINÁMICO (Buscador y Filtros) ---

    @Query("""
        SELECT u FROM Usuario u 
        WHERE u.rol.nombre = 'ENTRENADOR'
        AND u.activo = true
        AND (
            LOWER(u.nombres) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    List<Usuario> buscarEntrenadores(@Param("keyword") String keyword);

    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'ENTRENADOR' AND u.activo = true")
    List<Usuario> listarEntrenadoresActivos();

    List<Usuario> findByNombresContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombres, String descripcion);
}