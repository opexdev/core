package co.nilin.opex.utility.log.interceptor.decorator;


import co.nilin.opex.utility.log.interceptor.*;
import org.reactivestreams.*;
import org.slf4j.*;
import org.springframework.core.io.buffer.*;
import org.springframework.http.*;
import org.springframework.http.server.reactive.*;
import reactor.core.publisher.*;

import java.util.stream.*;

import static reactor.core.scheduler.Schedulers.*;

public class PayloadBufferServerHttpResponseDecorator extends ServerHttpResponseDecorator {
    private Logger log = LoggerFactory.getLogger(PayloadBufferServerHttpResponseDecorator.class);

    private String tracing;

    PayloadBufferServerHttpResponseDecorator(String tracing, ServerHttpResponse delegate) {
        super(delegate);
        this.tracing = tracing;
        final String headers = delegate.getHeaders().entrySet()
                .stream()
                .map(entry -> " " + entry.getKey() + ": [" + String.join(";", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
        if (log.isDebugEnabled()) {
            log.debug("{}-" +
                    "Response Status  : {} \n" +
                    "Response Headers  : \n" +
                    "{}", tracing, delegate.getRawStatusCode(), headers);
        }
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        final MediaType contentType = super.getHeaders().getContentType();
        if (LogUtils.legalLogMediaTypes.stream().anyMatch(mt -> mt.isCompatibleWith(contentType))) {
            if (body instanceof Mono) {
                final Mono<DataBuffer> monoBody = (Mono<DataBuffer>) body;
                return super.writeWith(monoBody.publishOn(single())
                        .map(dataBuffer -> LogUtils.loggingResponse(log, tracing, dataBuffer)));
            } else if (body instanceof Flux) {
                final Flux<DataBuffer> monoBody = (Flux<DataBuffer>) body;
                return super.writeWith(monoBody.publishOn(single())
                        .map(dataBuffer -> LogUtils.loggingResponse(log, tracing, dataBuffer)));
            }
        }
        return super.writeWith(body);
    }
}