package org.acme;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.semconv.SemanticAttributes;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

//Implementation based on io.quarkus.opentelemetry.runtime.exporter.otlp.EndUserSpanProcessor
@ApplicationScoped
public class CurrentIdentityAssociationEndUserSpanProcessor implements SpanProcessor {

    @Inject
    //Use CurrentIdentityAssociation instead of SecurityIdentity, so we can get the deferredIdentity
    protected CurrentIdentityAssociation securityIdentityAssociation;
    @ConfigProperty(name = "quarkus.otel.traces.eusp.enabled")
    boolean euspEnabled;

    @Override
    @ActivateRequestContext
    public void onStart(Context parentContext, ReadWriteSpan span) {
        //showcase only. (Prevent active at same time as EndUserSpanProcessor)
        if (euspEnabled) {
            return;
        }

        //access to identity must be done in a non-blocking way
        securityIdentityAssociation.getDeferredIdentity().subscribe().with(identity -> {
            span.setAllAttributes(
                    identity.isAnonymous()
                            ? Attributes.empty()
                            : Attributes.of(
                            SemanticAttributes.ENDUSER_ID,
                            identity.getPrincipal().getName(),
                            SemanticAttributes.ENDUSER_ROLE,
                            identity.getRoles().toString()));
        });
    }

    // Original implementation from io.quarkus.opentelemetry.runtime.exporter.otlp.EndUserSpanProcessor
    // Issue: "ActivateRequestContext" is for the moment where the "Runnable" is added, but not when the "securityIdentity" is accessed.
    //    @Override
    //    @ActivateRequestContext
    //    public void onStart(Context parentContext, ReadWriteSpan span) {
    //        managedExecutor.execute(
    //                () -> span.setAllAttributes(
    //                        securityIdentity.isAnonymous()
    //                                ? Attributes.empty()
    //                                : Attributes.of(
    //                                SemanticAttributes.ENDUSER_ID,
    //                                securityIdentity.getPrincipal().getName(),
    //                                SemanticAttributes.ENDUSER_ROLE,
    //                                securityIdentity.getRoles().toString())));
    //    }

    @Override
    public boolean isStartRequired() {
        return Boolean.TRUE;
    }

    @Override
    public void onEnd(ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return Boolean.FALSE;
    }

}
