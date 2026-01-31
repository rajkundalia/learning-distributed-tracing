package com.example.inventory.config;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public OtlpHttpSpanExporter otlpHttpSpanExporter(
            @Value("${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318}") String endpoint) {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(endpoint + "/v1/traces")
                .build();
    }
}
