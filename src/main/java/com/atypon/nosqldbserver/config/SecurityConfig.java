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
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final JWTService jwtService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();
        http.authorizeRequests().antMatchers(new String[]{"/login"}).permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/db/{schemaName}", "/db/schemas/*", "/db/{schemaName}/index").hasRole("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/db/{schemaName}", "/db/schemas/*").hasRole("ADMIN");
        http.authorizeRequests().antMatchers("/db/schemas/{schemaName}/export", "/users/**").hasRole("ADMIN");

        http.authorizeRequests().anyRequest().authenticated();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
        http.addFilter(getAuthenticationFilter());
        http.addFilterBefore(getAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement().sessionCreationPolicy(STATELESS);
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
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
