package co.nilin.opex.utility.log.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

public class PayloadServerWebExchangeDecorator extends ServerWebExchangeDecorator {

    private Logger log = LoggerFactory.getLogger(PayloadServerWebExchangeDecorator.class);

    private PayloadBufferServerHttpRequestDecorator requestDecorator;

    private PayloadBufferServerHttpResponseDecorator responseDecorator;

    public PayloadServerWebExchangeDecorator(String tracing, ServerWebExchange delegate) {
        super(delegate);
        requestDecorator = new PayloadBufferServerHttpRequestDecorator(tracing, delegate.getRequest());
        responseDecorator = new PayloadBufferServerHttpResponseDecorator(tracing, delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}