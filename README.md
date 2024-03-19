# Reproducer for the ContextNotActiveException originating from EndUserSpanProcessor

Class with issue: https://github.com/quarkusio/quarkus/blob/main/extensions/opentelemetry/runtime/src/main/java/io/quarkus/opentelemetry/runtime/exporter/otlp/EndUserSpanProcessor.java

## Exception / Stacktrace
```
jakarta.enterprise.context.ContextNotActiveException: RequestScoped context was not active when trying to obtain a bean instance for a client proxy of CLASS bean [class=io.quarkus.security.runtime.SecurityIdentityProxy, id=U3fuB4yO9MSr82V2xU36xFn98dk]
- you can activate the request context for a specific method using the @ActivateRequestContext interceptor binding
at io.quarkus.arc.impl.ClientProxies.notActive(ClientProxies.java:70)
at io.quarkus.arc.impl.ClientProxies.getSingleContextDelegate(ClientProxies.java:30)
at io.quarkus.security.runtime.SecurityIdentityProxy_ClientProxy.arc$delegate(Unknown Source)
at io.quarkus.security.runtime.SecurityIdentityProxy_ClientProxy.isAnonymous(Unknown Source)
at io.quarkus.opentelemetry.runtime.exporter.otlp.EndUserSpanProcessor.lambda$onStart$0(EndUserSpanProcessor.java:31)
at io.smallrye.context.impl.wrappers.SlowContextualRunnable.run(SlowContextualRunnable.java:19)
at io.quarkus.vertx.core.runtime.VertxCoreRecorder$14.runWith(VertxCoreRecorder.java:587)
at org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2513)
at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1538)
at org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:29)
at org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:29)
at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
at java.base/java.lang.Thread.run(Thread.java:1583)
```

## Steps to reproduce
1. Run the GreetingResourceTest
2. Check the logs for infos from the `org.acme.reproducer.ValidatingNoopSpanExporter`

_**Example:**_
```
Number of spans without enduser.id tag: 16
Number of spans with enduser.id tag: 84
```

## Explanation for the reproducer
To simplify the setup, this project uses a custom SpanExporter. The `ValidatingNoopSpanExporter` is a simple implementation that counts the number of spans with and without the `enduser.id` tag. (So we don't need to set up Jaeger or any other tracing system.)

The test `GreetingResourceTest` uses the EndUserSpanProcessor implementation from Quarkus.

The test `GreetingResourceWithAdjustedProcessorTest` uses a `CurrentIdentityAssociationEndUserSpanProcessor` instead of the EndUserSpanProcessor from Quarkus to set the `enduser.id` and `enduser.role` attributes.