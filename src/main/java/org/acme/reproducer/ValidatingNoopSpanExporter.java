package org.acme.reproducer;

import static org.jboss.logging.Logger.getLogger;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

@Unremovable
@ApplicationScoped
public class ValidatingNoopSpanExporter implements SpanExporter {

    private static final Logger LOGGER = getLogger(ValidatingNoopSpanExporter.class);
    static final AtomicInteger TAG_MISSING_COUNTER = new AtomicInteger(0);
    static final AtomicInteger TAG_SET_COUNTER = new AtomicInteger(0);


    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            if (span.getAttributes().get(AttributeKey.stringKey("enduser.id")) != null) {
                TAG_SET_COUNTER.incrementAndGet();
            } else {
                LOGGER.error("Span without enduser.id tag." + span);
                TAG_MISSING_COUNTER.incrementAndGet();
            }
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        LOGGER.info("Number of spans without enduser.id tag: " + TAG_MISSING_COUNTER.get());
        LOGGER.info("Number of spans with enduser.id tag: " + TAG_SET_COUNTER.get());
        return CompletableResultCode.ofSuccess();
    }

}
