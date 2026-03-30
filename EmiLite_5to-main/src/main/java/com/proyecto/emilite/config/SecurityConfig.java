package com.proyecto.emilite.config;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.proyecto.emilite.security.CustomAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 1. Rutas PÚBLICAS (sin autenticación)
                .requestMatchers(
                    "/",                       // Landing page
                    "/login",                  // Página de login
                    "/usuarios/registro",      // Página de registro (vieja?)
                    "/usuarios/registro-publico", // Página de registro público
                    "/usuarios/crear-publico", // Endpoint para procesar registro público
                    "/css/**",                 // Archivos CSS
                    "/js/**",                  // Archivos JavaScript
                    "/images/**",              // Imágenes
                    "/error"                   // Página de error genérica
                ).permitAll()

                // 2. Rutas solo para ADMIN
                .requestMatchers(
                    "/api/usuarios/**",        // API de usuarios
                    "/admin/usuarios",         // Lista de usuarios para admin
                    "/admin/usuarios/**",      // Gestión de usuarios para admin (nuevo, editar, eliminar, etc.)
                    "/admin/servicios",        // Lista de servicios para admin (si aplica)
                    "/admin/servicios/**",     // Gestión de servicios para admin (nuevo, editar, eliminar)
                    "/admin/promociones",      // Lista de promociones para admin (si aplica)
                    "/admin/promociones/**",   // Gestión de promociones para admin (nueva, editar, eliminar)
                    "/admin/pagos",            // Lista de pagos para admin (si aplica)
                    "/admin/pagos/**",         // Gestión de pagos para admin (nuevo, editar, eliminar)
                    "/admin/dashboard",        // Dashboard específico para admin
                    "/reportes",               // Reportes
                    "/reportes/**"             // Reportes específicos
                ).hasRole("ADMIN")

                // 3. Rutas solo para ENTRENADOR
                .requestMatchers(
                    "/entrenador/**"           // Todas las rutas de entrenador
                ).hasRole("ENTRENADOR")

                // 4. Rutas solo para CLIENTE (las más específicas primero)
                .requestMatchers(
                    "/cliente/perfil/editar",  // Editar perfil (cliente)
                    "/cliente/perfil"          // Ver perfil (cliente, aunque puede ser solo lectura)
                    // Agrega aquí otras rutas exclusivas para CLIENTE si las hay
                ).hasRole("CLIENTE")

                // 5. Rutas para CLIENTE Y ENTRENADOR (las más generales después)
                .requestMatchers(
                    "/dashboard",              // Dashboard principal (redirige según rol)
                    "/cliente/pagos",          // Ver pagos (solo lectura para cliente)
                    "/cliente/rutinas",        // Ver rutinas (solo lectura para cliente)
                    "/cliente/servicios"       // Ver servicios (solo lectura para cliente)
                    // Agrega aquí otras rutas comunes si aplica
                ).hasAnyRole("CLIENTE", "ENTRENADOR")

                // 6. Cualquier otra ruta requiere autenticación (rol mínimo)
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler()) // <-- Asegúrate de que este Bean exista
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true") // Asegúrate de que la vista login maneje este parámetro
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403") // Página de acceso denegado
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Asegúrate de que este método del repositorio exista y funcione correctamente
            // Debe usar JOIN FETCH o EAGER para cargar el Rol
            Usuario usuario = usuarioRepository.findByUserNameWithRol(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            // Asegúrate de que el rol tenga el prefijo "ROLE_"
            String role = usuario.getRol().getNombre();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            var authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
            );

            return new User(
                usuario.getUserName(),
                usuario.getPassword(),
                authorities
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(); // Asegúrate de que esta clase exista
    }
}