package com.example.jwtinit.controller;

import antlr.Token;
import com.example.jwtinit.dto.LoginDto;
import com.example.jwtinit.dto.TokenDto;
import com.example.jwtinit.jwt.JwtFilter;
import com.example.jwtinit.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    public static final String TOKEN_TYPE = "Bearer ";

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

        // LoginDto 의 username, password 를 이용해 UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // authenticationToken 을 이용해서 Authentication 객체 생성 
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // 생성된 Authentication 을 Security Context 에 저장

        String token = tokenProvider.createToken(authentication);

        HttpHeaders httpHeaders = new HttpHeaders(); // token 을 Header 에 저장
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, TOKEN_TYPE + token); // TokenDto 를 이용해서 Body 에 저장

        ResponseEntity<TokenDto> response = new ResponseEntity<>(new TokenDto(token), httpHeaders, HttpStatus.OK);

        return new ResponseEntity<>(new TokenDto(token), httpHeaders, HttpStatus.OK); // token 이 포함된 Header, Body 와 Http Status 함꼐 return
    }

}