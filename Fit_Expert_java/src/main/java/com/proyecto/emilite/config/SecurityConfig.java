package com.proyecto.emilite.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Ignoramos CSRF solo en APIs externas que no pueden enviar el token
                .ignoringRequestMatchers(
                    "/pagos/**", 
                    "/admin/pagos/crear-preferencia/**", 
                    "/admin/pagos/pago-exitoso/**",
                    "/catalogo/crear-preferencia",
                    "/catalogo/pago-exitoso/**",
                    "/api/chat/enviar",
                    "/api/generar-diagnostico",
                    "/generar-diagnostico",
                    "/api/notificaciones/**",
                    "/pagos/webhook"
                )
            )
            .authorizeHttpRequests(auth -> auth
                // 1. RUTAS PÚBLICAS (Registro y creación obligatorios sin logueo)
                .requestMatchers(
                    "/", "/login", "/registro", 
                    "/usuarios/registro-publico", 
                    "/usuarios/crear-publico", 
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/error"
                ).permitAll()

                // 2. RUTAS DE ADMIN
                .requestMatchers("/admin/**", "/api/usuarios/**", "/reportes/**").hasRole("ADMIN")
                .requestMatchers("/admin/usuarios/ver-cv-mongo/").hasRole("ADMIN")

                // 3. RUTAS DE ENTRENADOR
                .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")

                // 4. RUTAS DE CLIENTE
                .requestMatchers("/cliente/**", "/api/rutina-real/**", "/cliente/entrenador/**").hasRole("CLIENTE")
                .requestMatchers("/valoracion/**").authenticated()
                .requestMatchers("/generar-diagnostico").authenticated()


                // 5. RUTAS COMPARTIDAS Y API
                .requestMatchers("/dashboard").hasAnyRole("CLIENTE", "ENTRENADOR", "ADMIN")
                .requestMatchers("/api/chat/**", "/api/notificaciones/**", "/activar-entrenador").authenticated()
                .requestMatchers("/api/valoracion", "/api/generar-diagnostico").hasRole("CLIENTE")

                // 6. CUALQUIER OTRA RUTA REQUIERE LOGIN
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true) 
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Usuario usuario = usuarioRepository.findByUserNameWithRol(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            String nombreRol = usuario.getRol().getNombre().toUpperCase().trim();

            if (nombreRol.equals("ROLE_ENTRENADOR") && !usuario.isValidado()) {
                throw new DisabledException("Tu cuenta aún no ha sido aprobada.");
            }

            return new User(
                usuario.getUserName(),
                usuario.getPassword(),
                usuario.isEnabled(),
                true, true, true, 
                Collections.singletonList(new SimpleGrantedAuthority(nombreRol))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}