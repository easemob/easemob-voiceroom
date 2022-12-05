package service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Slf4j
@Component
public class VoiceRoomJwtUtil {

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Value("${jwt.token.exp-time}")
    private String exTime;

    public String getUid(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        Claims claims = parseJWT(token);
        if (claims == null) {
            return null;
        }
        return claims.get("uid", String.class);
    }

    private Claims parseJWT(String token) {
        Claims claims = null;
        try {
            if (StringUtils.isNotBlank(token)) {
                SecretKey key =
                        Keys.hmacShaKeyFor(Base64.getEncoder().encode(secretKey.getBytes()));
                claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            }else {
                log.warn("token is empty!");
            }
        } catch (Exception e) {
            log.error("token parse failed | cause:", e);
        }
        return claims;
    }

}
