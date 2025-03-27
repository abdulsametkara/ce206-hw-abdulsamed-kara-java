package com.samet.music.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsManager {
    private static final Logger logger = LoggerFactory.getLogger(MetricsManager.class);
    private static MetricsManager instance;
    private final PrometheusMeterRegistry registry;
    private HTTPServer server;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    private MetricsManager() {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        try {
            // HTTPServer sınıfının doğru yapıcısını kullanıyoruz
            server = new HTTPServer(new InetSocketAddress(8080), registry.getPrometheusRegistry(), true);
            logger.info("Prometheus metrics server started on port 8080");
        } catch (IOException e) {
            logger.error("Failed to start metrics server", e);
        }
    }

    public static synchronized MetricsManager getInstance() {
        if (instance == null) {
            instance = new MetricsManager();
        }
        return instance;
    }

    public void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        counters.computeIfAbsent(key, k -> Counter.builder(name)
                        .tags(tags)
                        .description(name + " counter")
                        .register(registry))
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key, k -> Timer.builder(name)
                .tags(tags)
                .description(name + " timer")
                .register(registry));
        sample.stop(timer);
    }

    public void recordExecutionTime(String name, long timeMs, String... tags) {
        String key = name + String.join("", tags);
        timers.computeIfAbsent(key, k -> Timer.builder(name)
                        .tags(tags)
                        .description(name + " timer")
                        .register(registry))
                .record(timeMs, TimeUnit.MILLISECONDS);
    }

    public MeterRegistry getRegistry() {
        return registry;
    }

    public void shutdown() {
        if (server != null) {
            server.stop();
        }
    }
}