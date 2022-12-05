package com.voiceroom.mic.common.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VoiceRoomJwtUtil {

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Value("${jwt.token.exp-time}")
    private String exTime;

    @Resource(name = "voiceRedisTemplate")
    private StringRedisTemplate redisTemplate;

    /**
     * 用户登录成功后生成Jwt
     * 使用Hs256算法  私匙使用用户密码
     *
     * @param
     * @return
     */
    public String createJWT(String uid) {
        String tokenKey = "user_token:" + uid;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
            Object jwtToken = redisTemplate.opsForValue().get(tokenKey);
            if (jwtToken != null) {
                return jwtToken.toString();
            }
        }
        Map<String, Object> claims = new HashMap<>(1);
        claims.put("uid", uid);
        SecretKey key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secretKey.getBytes()));
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setSubject(uid)
                .signWith(key, SignatureAlgorithm.HS256);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 10);
        Date exp = calendar.getTime();
        builder.setExpiration(exp);
        String result = builder.compact();
        redisTemplate.opsForValue().set(tokenKey, result, Integer.parseInt(exTime), TimeUnit.DAYS);
        return result;
    }

    public String getUid(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        Claims claims = parseJWT(token);
        if(claims==null){
            return null;
        }
        return claims.get("uid", String.class);
    }

    private Claims parseJWT(String token) {
        Claims claims = null;
        try {
            if (StringUtils.isNotBlank(token)) {
                //解析jwt
                SecretKey key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secretKey.getBytes()));
                claims = Jwts.parser().setSigningKey(key)
                        .parseClaimsJws(token).getBody();
            }else {
                log.warn("token is empty!");
            }
        } catch (Exception e) {
            log.error("token parse failed | cause:", e);
        }
        return claims;
    }

    public String getJwt(String uid) {
        String tokenKey = "user_token:" + uid;
        redisTemplate.expire(tokenKey, Integer.parseInt(exTime), TimeUnit.DAYS);
        return redisTemplate.opsForValue().get(tokenKey);
    }
}
