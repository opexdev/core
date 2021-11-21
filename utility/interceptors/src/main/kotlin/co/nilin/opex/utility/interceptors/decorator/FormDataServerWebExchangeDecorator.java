package co.nilin.opex.utility.interceptors.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

public class FormDataServerWebExchangeDecorator extends ServerWebExchangeDecorator {

    private Logger log = LoggerFactory.getLogger(FormDataServerWebExchangeDecorator.class);

    private FormDataServerHttpRequestDecorator requestDecorator;


    public FormDataServerWebExchangeDecorator(MultiValueMap<String, String> queryParams, ServerWebExchange delegate) {
        super(delegate);
        requestDecorator = new FormDataServerHttpRequestDecorator(queryParams, delegate.getRequest());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

}