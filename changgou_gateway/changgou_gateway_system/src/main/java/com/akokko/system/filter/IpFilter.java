package com.akokko.system.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class IpFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("经过第一个过滤器！");
        // 打印IP地址
        System.out.println(exchange.getRequest().getRemoteAddress().getHostName());
        // 放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 定义执行顺序
        return 1;
    }
}
