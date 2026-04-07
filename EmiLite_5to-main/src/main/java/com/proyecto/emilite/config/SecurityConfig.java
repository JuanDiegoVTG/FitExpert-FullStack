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
            // 1. IGNORAR CSRF: Vital para que Mercado Pago y el Fetch de JS funcionen
            .ignoringRequestMatchers(
                "/pagos/**", 
                "/admin/pagos/crear-preferencia/**", 
                "/admin/pagos/pago-exitoso/**"
            )
        )
        .authorizeHttpRequests(authorize -> authorize
            // 2. RUTAS PÚBLICAS: Acceso total sin loguearse
            .requestMatchers(
                "/", 
                "/login", 
                "/usuarios/registro-publico", 
                "/usuarios/crear-publico", 
                "/css/**", 
                "/js/**", 
                "/images/**", 
                "/error",
                "/pagos/respuesta", 
                "/pagos/webhook"
            ).permitAll()

            // 3. ⚡ EXCEPCIÓN DE PASARELA (EL ARREGLO DEL ERROR 414):
            // Permitimos que el CLIENTE entre a estas rutas de "admin" específicas para pagar.
            // DEBEN IR ANTES de la restricción general de /admin/**
            .requestMatchers("/admin/pagos/crear-preferencia/**").hasAnyRole("ADMIN", "CLIENTE")
            .requestMatchers("/admin/pagos/pago-exitoso/**").hasAnyRole("ADMIN", "CLIENTE")

            // 4. RUTAS DE ADMIN: Solo personal autorizado
            .requestMatchers("/admin/**", "/api/usuarios/**", "/reportes/**").hasRole("ADMIN")

            // 5. RUTAS DE ENTRENADOR
            .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")

            // 6. RUTAS DE CLIENTE
            .requestMatchers("/cliente/**", "/api/rutina-real/**").hasRole("CLIENTE")

            // 7. RUTAS COMPARTIDAS
            .requestMatchers("/dashboard").hasAnyRole("CLIENTE", "ENTRENADOR", "ADMIN")

            .anyRequest().authenticated()
        )
        .formLogin(formLogin -> formLogin
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
        .exceptionHandling(exception -> exception
            .accessDeniedPage("/error/403")
        );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Usuario usuario = usuarioRepository.findByUserNameWithRol(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            // Validación de Entrenador aprobado
            if (usuario.getRol().getNombre().equals("ENTRENADOR") && !usuario.isValidado()) {
                throw new DisabledException("Tu cuenta aún no ha sido aprobada por el administrador.");
            }

            // Normalización del rol para Spring Security (Asegura prefijo ROLE_)
            String roleName = usuario.getRol().getNombre().toUpperCase().trim(); 
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            var authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

            return new User(
                usuario.getUserName(),
                usuario.getPassword(),
                usuario.isEnabled(), 
                true, true, true, 
                authorities
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}