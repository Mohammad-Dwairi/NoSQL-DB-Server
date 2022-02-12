package com.atypon.nosqldbserver.config;

import com.atypon.nosqldbserver.security.filter.AppAuthenticationEntryPoint;
import com.atypon.nosqldbserver.security.filter.AppAuthenticationFilter;
import com.atypon.nosqldbserver.security.filter.AppAuthorizationFilter;
import com.atypon.nosqldbserver.security.handler.AppAccessDeniedHandler;
import com.atypon.nosqldbserver.security.handler.AppAuthenticationFailureHandler;
import com.atypon.nosqldbserver.security.handler.AppAuthenticationSuccessHandler;
import com.atypon.nosqldbserver.security.jwt.JWTService;
import com.atypon.nosqldbserver.security.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;



@Profile("master")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class MasterSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final JWTService jwtService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().configurationSource(corsConfigurationSource());
        http.headers().frameOptions().disable();
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/db/replica/**").hasRole("NODE");
        http.authorizeRequests().antMatchers("/db/sync/**").hasRole("NODE");
        http.authorizeRequests().antMatchers("/db/ddl-write/**", "/db/users/**").hasRole("ADMIN");
        http.authorizeRequests().anyRequest().authenticated();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
        http.addFilter(getAuthenticationFilter());
        http.addFilterBefore(getAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement().sessionCreationPolicy(STATELESS);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AppAuthenticationFailureHandler();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AppAuthenticationSuccessHandler(jwtService);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AppAuthenticationEntryPoint();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AppAccessDeniedHandler();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowOrigins = Arrays.asList("*");
        configuration.setAllowedOriginPatterns(allowOrigins);
        configuration.setAllowedMethods(singletonList("*"));
        configuration.setAllowedHeaders(singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    private AppAuthenticationFilter getAuthenticationFilter() throws Exception {
        return AppAuthenticationFilter.builder()
                .authenticationManager(authenticationManagerBean())
                .authenticationSuccessHandler(authenticationSuccessHandler())
                .authenticationFailureHandler(authenticationFailureHandler()).build();
    }

    private AppAuthorizationFilter getAuthorizationFilter() {
        return new AppAuthorizationFilter(jwtService);
    }
}
