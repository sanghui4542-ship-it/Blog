package com.shinsunyoung.blog.service;

import com.shinsunyoung.blog.domain.RefreshToken;
import com.shinsunyoung.blog.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    //리플러시 토큰을 DB의 RefreshToken테이블에서 조회할 리파지터리 객체 생성자 자동 주입
    private final RefreshTokenRepository refreshTokenRepository;

    //==== findByRefreshToken() : 요청한 리플러시 토큰 문자열을 매개변수로 받아  DB에서 조회한 토큰의 행을 조회해서 반환
    // 돌려주는 값: 조회한 토큰 행 정보가 저장된  RefreshToken 엔티티 객체 (그 안에 요청한 사용자 id 들어 있습니다)
    // 조회하지 못하면? "Unexpected token" 오류를 던집니다.
    public RefreshToken findByRefreshToken(String refreshToken){

        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException("Unexpected token"));
    }



}
