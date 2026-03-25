package com.epidemic.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 转发客户端IP的全局过滤器
 * 将客户端真实IP添加到X-Forwarded-For和X-Real-IP header中
 */
@Component
public class ClientIpForwardFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remoteAddress = request.getRemoteAddress();

        if (remoteAddress != null) {
            String clientIp = remoteAddress.getAddress().getHostAddress();
            // 处理IPv6回环地址的情况
            if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
                clientIp = "127.0.0.1";
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Forwarded-For", clientIp)
                    .header("X-Real-IP", clientIp)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}