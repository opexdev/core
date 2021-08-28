package co.nilin.opex.utility.interceptor;

import co.nilin.opex.utility.interceptor.decorator.*;
import org.springframework.http.codec.multipart.*;
import org.springframework.http.server.reactive.*;
import org.springframework.util.*;
import org.springframework.web.server.*;
import reactor.core.publisher.*;

import java.util.*;


public class FormDataWorkaroundFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		final ServerHttpRequest request = exchange.getRequest();

		final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

		//add all content from form data to query params
		exchange.getFormData().subscribe(queryParams::putAll);

		exchange.getMultipartData().subscribe(map -> {
			map.forEach((key, value) -> {
				List<?> list = value;
				list.forEach(item -> {
					//add each form field parts to query params
					if (item instanceof FormFieldPart) {
						final FormFieldPart formFieldPart = (FormFieldPart) item;
						queryParams.add(key, formFieldPart.value());
					}
				});

			});
		});

		//add original query params to win identical name war
		queryParams.putAll(request.getQueryParams());

		return chain.filter(new FormDataServerWebExchangeDecorator(queryParams, exchange));
	}


}
