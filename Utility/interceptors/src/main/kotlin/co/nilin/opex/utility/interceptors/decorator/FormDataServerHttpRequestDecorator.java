package co.nilin.opex.utility.interceptors.decorator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;

public class FormDataServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private MultiValueMap<String, String> queryParams;

    FormDataServerHttpRequestDecorator(MultiValueMap<String, String> queryParams, ServerHttpRequest delegate) {
        super(delegate);
        this.queryParams = queryParams;
    }

    @Override
    public MultiValueMap<String, String> getQueryParams() {
        return queryParams;
    }
}