package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ContextProvider {
    ChatContext getContext(Update update);

    void saveContext(Update update, ChatContext ctx);
}