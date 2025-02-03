package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdatePrePostProcessor {
    void preProcess(Update update,
                    DefaultAbsSender sender);

    void postProcess(Update update,
                     DefaultAbsSender sender);
}