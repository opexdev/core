package co.nilin.opex.utility.interceptors;

import co.nilin.opex.utility.interceptors.decorator.FormDataServerWebExchangeDecorator;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class FormDataWorkaroundFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        return exchange.getFormData()
                .doOnNext(queryParams::putAll)
                .flatMap(it ->
                        exchange.getMultipartData().map(map -> {
                            map.forEach((key, value) -> {
                                value.forEach(item -> {
                                    //add each form field parts to query params
                                    if (item instanceof FormFieldPart) {
                                        final FormFieldPart formFieldPart = (FormFieldPart) item;
                                        queryParams.add(key, formFieldPart.value());
                                    }
                                });

                            });
                            return map;
                        })
                )
                .doOnNext(it ->
                        queryParams.putAll(request.getQueryParams()))
                .flatMap(it ->
                        chain.filter(new FormDataServerWebExchangeDecorator(queryParams, exchange))
                );
    }

}
