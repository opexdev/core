package co.nilin.opex.utility.interceptor.decorator;

import org.springframework.http.server.reactive.*;
import org.springframework.util.*;

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