package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ExceptionHandler {
    void handle(Update update, Throwable t);
}