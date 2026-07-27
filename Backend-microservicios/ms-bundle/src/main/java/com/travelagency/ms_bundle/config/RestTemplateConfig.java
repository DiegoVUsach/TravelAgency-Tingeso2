package com.travelagency.ms_bundle.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with @LoadBalanced annotation.
     *
     * WHY @LoadBalanced?
     * - Without it, RestTemplate only understands real URLs like http://localhost:8081/...
     * - With @LoadBalanced, RestTemplate can resolve Eureka service names:
     *   http://ms-reservation/api/v1/... → Eureka looks up "ms-reservation" and replaces
     *   it with the actual host:port (e.g., http://192.168.1.5:54321/api/v1/...)
     *
     * This is essential because microservices use server.port=0 (random ports),
     * so we can't hardcode addresses. Eureka + @LoadBalanced handles this automatically.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
