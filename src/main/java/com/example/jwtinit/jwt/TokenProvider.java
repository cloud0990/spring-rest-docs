package com.example.jwtinit.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

// TokenProvider: 토큰의 생성, 유효성 검증 등을 담당
// 1. Override afterPropertiesSet(): 주입 받은 secret 값을 BASE64 로 Decode 후, key(jwt) 변수에 할당
// 2. createToken(): Authentication (권한) 객체를 이용해 Token 생성
// 3. getAuthentication(): Token 을 사용해 Authentication 객체 얻기 ( return 유저객체, 토근, 권핞 정보 )
// 4. validateToken(): Token 의 유효성 검증
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";
    private final String secret;
    private final long tokenValidityInMilliseconds;

    private Key key;

    public TokenProvider(@Value("${jwt.secret}") String secret
                        , @Value("${jwt.token-validity-in-seconds}") long tokenValidityInMilliseconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds * 1000;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // 주입 받은 secret 값을 BASE64 decode 후, key 변수에 할당
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Authentication 객체를 이용해 Token 생성
    public String createToken(Authentication authentication) {

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                // payload 구성
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(validity) // 토큰 발행 일자 저장
                
                // signature 구성
//                .signWith(key, SignatureAlgorithm.HS512) // signWith(암호화 알고리즘, 암복호화에 사용할 키)
                .signWith(key, SignatureAlgorithm.HS512) // signWith(암호화 알고리즘, 암복호화에 사용할 키)
                .compact() // 토큰 생성
                ;
    }

    // Token 정보를 이용해 Authentication 객체 리턴
    //      Token 으로 Claim 생성 후, 이를 이욯해 유저 객체 생성해서 Authentication 객체 리턴
    public Authentication getAuthentication(String token) {

        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();


        Collection< ? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

         User principal = new User(claims.getSubject(), "", authorities);

        //                                             유저객체     토큰   권한정보
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // Token 유효성 검증 수행
    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("TokenProvider validateToken() SecurityException | MalformedJwtException {} 잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("TokenProvider validateToken() UnsupportedJwtException {} 만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("TokenProvider validateToken() UnsupportedJwtException {} 지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("TokenProvider validateToken() IllegalArgumentException {} JWT 토큰이 잘못되었습니다.");
        } catch (Exception e) {
            log.info("TokenProvider validateToken() Exception {} Exception");
        }

        return false;
    }

}