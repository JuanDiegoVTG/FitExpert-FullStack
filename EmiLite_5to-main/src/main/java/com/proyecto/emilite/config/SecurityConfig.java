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
                "/admin/pagos/pago-exitoso/**",
                "/catalogo/crear-preferencia",
                "/catalogo/pago-exitoso/**",
                "/api/chat/enviar",
                "/api/generar-diagnostico"
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
                "/catalogo/**",
                "/catalogo/pago-exitoso/**",
                "/pagos/respuesta", 
                "/pagos/webhook"
            ).permitAll()

            // 3. EXCEPCIÓN DE PASARELA 
            // Permitimos que el CLIENTE entre a estas rutas de "admin" específicas para pagar.
            .requestMatchers("/admin/pagos/crear-preferencia/**").hasAnyRole("ADMIN", "CLIENTE")
            .requestMatchers("/admin/pagos/pago-exitoso/**").hasAnyRole("ADMIN", "CLIENTE")
            
            //RUTAS DE CHAT (Para que ambos roles entren)
            .requestMatchers("/api/chat/**").authenticated()

            // 4. RUTAS DE ADMIN: Solo personal autorizado
            .requestMatchers("/admin/**", "/api/usuarios/**", "/reportes/**").hasRole("ADMIN")

            // 5. RUTAS DE ENTRENADOR
            .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")

            // 6. RUTAS DE CLIENTE
            .requestMatchers("/cliente/**", "/api/rutina-real/**","/cliente/entrenador/" ).hasRole("CLIENTE")

            // 7. RUTAS COMPARTIDAS
            .requestMatchers("/dashboard").hasAnyRole("CLIENTE", "ENTRENADOR", "ADMIN")

            // 8. Rutas del Diagnóstico (Solo para Clientes)
            .requestMatchers("/api/valoracion", "/api/generar-diagnostico").hasRole("CLIENTE")              

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

            // 🕵️ DEBUG: Esto saldrá en tu consola de Eclipse/VS Code
            System.out.println("DEBUG LOGIN - Usuario: " + username);
            System.out.println("DEBUG LOGIN - Rol: " + usuario.getRol().getNombre());
            System.out.println("DEBUG LOGIN - Validado: " + usuario.isValidado());

            // 🛡️ VALIDACIÓN REFORZADA
            String nombreRol = usuario.getRol().getNombre().toUpperCase().trim();
            
            if (nombreRol.equals("ENTRENADOR") && !usuario.isValidado()) {
                System.out.println("🛑 BLOQUEO ACTIVADO para: " + username);
                throw new DisabledException("Tu cuenta aún no ha sido aprobada por el administrador.");
            }

            // ... resto del código de authorities ...
            String roleName = "ROLE_" + nombreRol;
            return new User(
                usuario.getUserName(),
                usuario.getPassword(),
                usuario.isEnabled(), // Usa el campo que pusimos en la entidad
                true, true, true, 
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}