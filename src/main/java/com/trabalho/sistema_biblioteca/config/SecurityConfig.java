
package com.trabalho.sistema_biblioteca.config;

import com.trabalho.sistema_biblioteca.model.enums.Status;
import com.trabalho.sistema_biblioteca.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UsuarioRepository usuarios) {

        return email -> {
            var usuario = usuarios.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Credenciais inválidas"));

            return User.withUsername(usuario.getEmail())
                    .password(usuario.getSenha())
                    .roles(usuario.getTipo().name())
                    .disabled(usuario.getStatus() != Status.ATIVO)
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Preserva a resposta HTTP original em despachos de erro.
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()

                        // Cadastro de usuários
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios")
                        .permitAll()

                        // Consulta pública do acervo
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/livros", "/api/livros/**",
                                "/api/autores", "/api/autores/**",
                                "/api/categorias", "/api/categorias/**")
                        .permitAll()

                        // Cadastro do acervo: bibliotecários
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/livros", "/api/livros/**",
                                "/api/autores", "/api/autores/**",
                                "/api/categorias", "/api/categorias/**")
                        .hasRole("BIBLIOTECARIO")

                        // Alteração e exclusão de livros
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/livros/**")
                        .hasRole("BIBLIOTECARIO")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/livros/**")
                        .hasRole("BIBLIOTECARIO")

                        // Devoluções e multas
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/emprestimos/*/devolver",
                                "/api/emprestimos/*/quitar-multa")
                        .hasRole("BIBLIOTECARIO")

                        // Histórico administrativo
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/emprestimos/usuarios/**")
                        .hasRole("BIBLIOTECARIO")

                        // Demais rotas exigem autenticação
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
