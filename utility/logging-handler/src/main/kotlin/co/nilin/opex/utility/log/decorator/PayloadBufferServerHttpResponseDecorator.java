package co.nilin.opex.utility.log.decorator;


import co.nilin.opex.utility.log.LogUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static reactor.core.scheduler.Schedulers.single;

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