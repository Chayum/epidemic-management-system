package com.epidemic.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登出响应处理过滤器工厂
 * 用于拦截登出接口响应，删除 Redis 中的 Token 或加入黑名单
 */
@Component
@Slf4j
public class LogoutTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LogoutResponseRewriteFunction rewriteFunction;

    public LogoutTokenGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> chain.filter(exchange)
                .then(rewriteFunction.apply(exchange));
    }
}
