package com.example.jwtinit.config;

import antlr.Token;
import com.example.jwtinit.jwt.JwtAccessDeniedHandler;
import com.example.jwtinit.jwt.JwtAuthenticationEntryPoint;
import com.example.jwtinit.jwt.JwtSecurityConfig;
import com.example.jwtinit.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity // 웹 보안 활성화
//                          @PreAuthorize 를 메소드 단위로 추가하기 위함
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@EnableGlobalMethodSecurity(prePostEnabled = true, secureEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(TokenProvider tokenProvider, JwtAccessDeniedHandler jwtAccessDeniedHandler, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.tokenProvider = tokenProvider;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    // 비밀번호 암호화
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    // WebSecurity: 패턴에 해당하는 리소스를 Spring Security 설정에서 아예 무시
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers(
                        "/h2-console/**",
                        "/favicon.ico",
                        "/resources/static/**",
                        "/static/**"
                );
    }

    @Override
    // HttpSecurity: WebSecurity 에서 제외된 리소스 외의 부분에 Spring Security 설정 가능
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()

                // Spring Filter 에서 발생하는 인증/인가 관련 에러 처리 설정
                .exceptionHandling()
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)

                // H2 DB 설정
                .and()
                .headers()
                    .frameOptions()
                    .sameOrigin()

                // 세션 관리 방법 설정
                .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 사용 (세션 설정을 Stateless(= 서버가 상태를 보관하지 않음) 로 설정)

                // 인증/인가가 필요한 path 설정
                // hasRole(), permitAll(), authenticated() ... : 권한 범위 설정
                .and()
                .authorizeRequests() // HttpServletRequest 를 사용하는 요청들에 대한 접근 제한 설정
                    .antMatchers("/").permitAll()
                    .antMatchers("/index").permitAll()
                    .antMatchers("/api/authenticate").permitAll() // 토큰을 받기 위한 로그인 API
                    .antMatchers("/api/signup").permitAll() // 회원가입 API
                    .anyRequest().authenticated() // 나머지 요청에 대해서는 모두 인증 받아야함

                .and()
                .apply(new JwtSecurityConfig(tokenProvider))
        ;
    }

}