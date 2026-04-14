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

    // --- SEGURIDAD Y LOGIN ---

    // Crucial para el DashboardController y Login
    Optional<Usuario> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Carga el usuario y su rol en una sola consulta (Optimiza rendimiento)
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.userName = :userName")
    Optional<Usuario> findByUserNameWithRol(@Param("userName") String userName);


    // --- BÚSQUEDAS POR ROL Y ESTADO ---

    // Para listar todos los entrenadores o clientes activos
    List<Usuario> findByRolNombre(String rolNombre);

    List<Usuario> findByRol_Nombre(String nombre);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByRolNombreAndActivo(String rolNombre, Boolean activo);


    // --- LÓGICA DE VÍNCULOS (IA y Contratos) ---

    // Para que el entrenador vea a sus clientes asignados
    List<Usuario> findByEntrenadorId(Long id);


    // --- 📋 CATÁLOGO (Integración con Karol) ---

    /**
     * Busca entrenadores activos por nombre, apellido o descripción.
     * Ignora mayúsculas/minúsculas para que la búsqueda sea más amigable.
     */
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

    // Listar todos los entrenadores para el catálogo inicial
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'ENTRENADOR' AND u.activo = true")
    List<Usuario> listarEntrenadoresActivos();

    
}