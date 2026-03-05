package com.epidemic.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 登录响应处理过滤器工厂
 * 用于拦截登录接口响应，生成Token并重写响应体
 */
@Component
public class LoginTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFactory;

    @Autowired
    private LoginResponseRewriteFunction rewriteFunction;

    public LoginTokenGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return modifyResponseBodyFactory.apply(c -> c.setRewriteFunction(Map.class, Map.class, rewriteFunction));
    }
}
