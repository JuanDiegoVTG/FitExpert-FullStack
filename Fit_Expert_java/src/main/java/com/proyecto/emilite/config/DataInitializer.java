package com.proyecto.emilite.config;

import com.proyecto.emilite.model.Rol;       
import com.proyecto.emilite.model.Usuario;   
import com.proyecto.emilite.repository.RolRepository;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        return args -> {
            
            // 1. POBLADO DE ROLES (Usando setters para evitar líos de constructor)
            if (rolRepository.count() == 0) {
                Rol adminRol = new Rol();
                adminRol.setNombre("ADMIN");
                adminRol.setDescripcion("Administrador general");
                adminRol.setActivo(true);
                
                Rol entrenadorRol = new Rol();
                entrenadorRol.setNombre("ENTRENADOR");
                entrenadorRol.setDescripcion("Entrenador de planta");
                entrenadorRol.setActivo(true);
                
                Rol clienteRol = new Rol();
                clienteRol.setNombre("CLIENTE");
                clienteRol.setDescripcion("Usuario común");
                clienteRol.setActivo(true);
                
                rolRepository.save(adminRol);
                rolRepository.save(entrenadorRol);
                rolRepository.save(clienteRol);
                System.out.println("🌱 Roles base insertados con éxito.");
            }

            // 2. POBLADO DEL ADMINISTRADOR
            if (usuarioRepository.findByEmail("admin@fitexpert.com").isEmpty()) {
                Usuario admin = new Usuario();
                
                // 🔥 NOTA: Si te sigue saliendo error en setNombre, cámbialo por el atributo 
                // real de tu clase (ej: admin.setNombres("...") o admin.setNombreUsuario("..."))
                admin.setUserName("Admin Principal SENA");
                admin.setEmail("admin@fitexpert.com");
                admin.setPassword("AdminSena2026*"); 
                
                // Buscamos el rol de la lista que devuelve tu repositorio
                List<Rol> listaRoles = rolRepository.findByNombre("ADMIN");
                if (!listaRoles.isEmpty()) {
                    // Le asignamos el primer rol encontrado
                    admin.setRol(listaRoles.get(0)); 
                }
                
                usuarioRepository.save(admin);
                System.out.println("👑 Administrador por defecto creado: admin@fitexpert.com");
            }
        };
    }
}