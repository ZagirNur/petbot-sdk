package dev.zagirnur.petbot.sdk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerRegistryConfig {

    @Bean
    public HandlerRegistry handlerRegistry() {
        return new HandlerRegistry();
    }
}