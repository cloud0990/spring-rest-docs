package com.example.jwtinit.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
public class SecurityUtil {

    public SecurityUtil() {
    }

    // Security Context 에서 Authentication 객체를 이용해, username return
    public static Optional<String> getCurrUsername() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            log.info("Security Util getCurrUsername(): Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;

        // instanceof: 객체 타입 확인 연산자 (cf. js typeof)
        // 형변환 가능 여부를 확인하여 boolean 결과 return (주로 상속 관계에서 부모 or 자식 객체인지 확인하는데 사용됨)
        
        // getPrincipal(): 인증 중인 Principal or Authentication 반환
        if(authentication.getPrincipal() instanceof UserDetails) {

            log.info("SecurityUtil getCurrUsername authentication.getPrincipal(): {}", authentication.getPrincipal());

            UserDetails user = (UserDetails) authentication.getPrincipal();
            username = user.getUsername();

        }else if(authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(username);
    }

}