package dev.zagirnur.petbot.sdk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Автоконфигурация бота.
 * Будет создавать основной бин для взаимодействия с Telegram.
 */
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramBotAutoConfiguration {
}