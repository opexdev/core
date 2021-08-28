package co.nilin.opex.utility.log.interceptor.decorator;

import co.nilin.opex.utility.log.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;


import java.util.Optional;
import java.util.stream.Collectors;

import static reactor.core.scheduler.Schedulers.single;

public class PayloadBufferServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private Flux<DataBuffer> body;
    private Logger log = LoggerFactory.getLogger(PayloadBufferServerHttpRequestDecorator.class);

    PayloadBufferServerHttpRequestDecorator(String tracing, ServerHttpRequest delegate) {
        super(delegate);
        final String path = delegate.getURI().getPath();
        final String query = delegate.getURI().getQuery();
        final String method = Optional.ofNullable(delegate.getMethod()).orElse(HttpMethod.GET).name();
        final String headers = delegate.getHeaders().entrySet()
                .stream()
                .map(entry -> "            " + entry.getKey() + ": [" + String.join(";", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
        final MediaType contentType = delegate.getHeaders().getContentType();
        if (log.isDebugEnabled()) {
            log.debug("{}-" +
                    "HttpMethod : {}\n" +
                    "Uri        : {}\n" +
                    "Headers    : \n" +
                    "{}", tracing, method, path + (StringUtils.hasText(query) ? "?" + query : ""), headers);
        }
        Flux<DataBuffer> flux = super.getBody();
        if (LogUtils.legalLogMediaTypes.contains(contentType)) {
            body = flux.publishOn(single()).map(dataBuffer -> LogUtils.loggingRequest(log, tracing, dataBuffer));
        } else {
            body = flux;
        }
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return body;
    }

}