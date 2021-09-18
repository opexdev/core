package co.nilin.opex.utility.interceptor.decorator;

import org.slf4j.*;
import org.springframework.http.server.reactive.*;
import org.springframework.util.*;
import org.springframework.web.server.*;

public class FormDataServerWebExchangeDecorator extends ServerWebExchangeDecorator {

    private Logger log = LoggerFactory.getLogger( FormDataServerWebExchangeDecorator.class);

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