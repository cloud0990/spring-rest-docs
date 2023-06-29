package com.example.jwtinit.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer ";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    // 실제 필터링 로직 수행
    // JWT 토큰(TokenProvider)의 인증정보를 SecurityContext 에 저장하는 역할 수행
    // getToken() 을 통해 토큰을 받아와서 유효성 검증 후, 정상 토큰이면 SecurityContext 에 저장
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = getToken(httpServletRequest);

        String requestURI = httpServletRequest.getRequestURI();

        if(StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

            Authentication authentication = tokenProvider.getAuthentication(token); // 토큰 유효성 검증

            SecurityContextHolder.getContext().setAuthentication(authentication); // Security Context 저장

            log.info("JwtFilter doFilter(): Security Context에 '{}' 인증 정보 저장. URI: {}", authentication.getName(), requestURI);

        }else {
            log.info("JwtFilter doFilter(): 유효한 JWT 토큰이 없습니다. URI: {}", requestURI);
        }

        chain.doFilter(request, response);
    }

    // Request Header 에서 토큰 정보를 꺼내옴
    private String getToken(HttpServletRequest request) {

        String token = request.getHeader(AUTHORIZATION_HEADER);

        log.info("JwtFilter getToken(): header = {}", token);
        //     Token type
        // ex. Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXJhbSIsImF1dGgiOiJST0xFX1VTRVIiLCJpYXQiOjE2ODc0MjMzNzh9.oiQU2sWKxNNjg4T-V6bZXpm88puZLO6-NACi1a8vDyLwalYrXZX-isphPbp_VIN5wkU0976KvvcvmsweEOKYhA

        // token 과 일치하고, TOKEN_TYPE 으로 시작하면
        if(StringUtils.hasText(token) && token.startsWith(TOKEN_TYPE)) {
            return token.substring(TOKEN_TYPE.length());
        }

        return null;
    }


}