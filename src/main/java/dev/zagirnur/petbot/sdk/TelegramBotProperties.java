package dev.zagirnur.petbot.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {

    /**
     * Токен бота. Обязательный параметр.
     */
    private String token;

    /**
     * Имя бота (может использоваться для внутренних целей).
     */
    private String username;

    // Геттеры/сеттеры

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}