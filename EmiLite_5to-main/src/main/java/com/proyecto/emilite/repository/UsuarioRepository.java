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
    // Método para encontrar un usuario por nombre de usuario
    Optional<Usuario> findByUserName(String userName);

    // Comprueba existencia por nombre de usuario
    boolean existsByUserName(String userName);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.userName = :userName") // JPQL con JOIN FETCH
    Optional<Usuario> findByUserNameWithRol(@Param("userName") String userName);

    // Buscar usuarios por nombre de rol
    List<Usuario> findByRolNombre(String rolNombre);

    // Buscar por estado activo
    List<Usuario> findByActivo(Boolean activo);

    // Buscar por nombre de rol y estado activo
    List<Usuario> findByRolNombreAndActivo(String rolNombre, Boolean activo);

    Optional<Usuario> findByEmail(String email);

    // Comprueba existencia por email
    boolean existsByEmail(String email);
}