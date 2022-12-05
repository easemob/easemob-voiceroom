package service.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.common.BaseResult;
import service.utils.VoiceRoomJwtUtil;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenFilter implements GlobalFilter , Ordered {

    private String[] skipAuthUrls = {"/user/login/device", "/voice/management/health"};

    @Resource
    private StringRedisTemplate redisTemplate;

    @Value("${jwt.token.exp-time}")
    private String exTime;

    @Resource
    private VoiceRoomJwtUtil voiceRoomJwtUtil;

    /**
     * 过滤器
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();
        if (null != skipAuthUrls && Arrays.asList(skipAuthUrls).contains(url)) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        ServerHttpResponse resp = exchange.getResponse();
        if (null == token || token.isEmpty()) {
            return authError(resp, "no login!");
        } else {
            try {
                if (token.startsWith("Bearer")) {
                    token = token.substring(7);
                }
                String uid = voiceRoomJwtUtil.getUid(token);
                ServerHttpRequest request = null;
                if (StringUtils.isNotBlank(uid)) {
                    request = exchange.getRequest().mutate().build();
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(exchange.getRequest().getHeaders());
                    headers.set("uid", uid);
                    request = new ServerHttpRequestDecorator(request) {
                        @Override public HttpHeaders getHeaders() {
                            return headers;
                        }
                    };
                }
                String tokenKey = "user_token:" + uid;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
                    return authError(resp, "auth error, the token not found!");
                } else {
                    String redisToken = redisTemplate.opsForValue().get(tokenKey);
                    if (!token.equals(redisToken)) {
                        return authError(resp, "auth error, the token is expired!");
                    }
                    //  更新token过期时间
                    redisTemplate.opsForValue()
                            .set(tokenKey, token, Integer.parseInt(exTime), TimeUnit.DAYS);
                }
                return request == null ?
                        chain.filter(exchange) :
                        chain.filter(exchange.mutate().request(request).build());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return authError(resp, "auth failed!");
            }
        }
    }
 
    /**
     * 认证错误输出
     *
     * @param resp 响应对象
     * @param message 错误信息
     * @return
     */
    private Mono<Void> authError(ServerHttpResponse resp, String message) {
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String returnStr = JSON.toJSONString(BaseResult.error(401, message));
        DataBuffer buffer = resp.bufferFactory().wrap(returnStr.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
