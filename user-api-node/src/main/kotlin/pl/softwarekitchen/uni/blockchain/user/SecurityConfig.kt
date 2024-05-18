package pl.softwarekitchen.uni.blockchain.user

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun userDetailsService(): UserDetailsService {
        val userDetailsList = listOf<UserDetails>(
            User.withDefaultPasswordEncoder().username("user1").password("password1").roles("USER").build(),
            User.withDefaultPasswordEncoder().username("user2").password("password2").roles("USER").build(),
            User.withDefaultPasswordEncoder().username("user3").password("password3").roles("USER").build()
        )
        return InMemoryUserDetailsManager(userDetailsList)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/user/**").authenticated() // Authenticate requests under /user/*
                    .anyRequest().permitAll() // Allow all other requests
            }
            .httpBasic()
        return http.build()
    }
}