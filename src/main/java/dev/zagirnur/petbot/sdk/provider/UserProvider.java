package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserProvider {
    BotUser getUser(Update update);
}