package co.nilin.opex.utility.log.interceptor;

import co.nilin.opex.utility.log.interceptor.decorator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(2)
public class RequestFilter implements WebFilter {

    private Logger log = LoggerFactory.getLogger( RequestFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if ( exchange.getRequest().getPath().toString().startsWith("/actuator/health"))
            return chain.filter(exchange);
        long startTime = System.currentTimeMillis();
        String tracing = UUID.randomUUID().toString();
        return chain.filter(new PayloadServerWebExchangeDecorator(tracing, exchange))
                .doOnSuccess((done) -> success(tracing, startTime));
    }

    private void success(String tracing, long startTime) {
        log.info("{}-Response Time：{} s", tracing, (System.currentTimeMillis() - startTime) / 1000.0);
    }

}