package com.assignment.ExchangeApplication.configuration;

import com.assignment.ExchangeApplication.repository.AccountRepository;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.ClientServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public UserDetailsService userDetailsService(ClientRepository clientRepository,
                                                 AccountRepository accountRepository,
                                                 PasswordEncoder passwordEncoder) {
        return new ClientServiceImpl(clientRepository, accountRepository, passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .httpBasic(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/error").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/client/register").anonymous()
                                .requestMatchers("/hello").authenticated()
                                .requestMatchers(HttpMethod.GET,"/api/client/").authenticated()
                                .requestMatchers(HttpMethod.POST,"/api/account/create").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/account/all").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/account/{clientId}").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/transaction/deposit").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/transaction/withdraw").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/transaction/transfer").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/transaction/{accountId}").authenticated()

                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session

                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            ClientRepository clientRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService(clientRepository, accountRepository, passwordEncoder));
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


