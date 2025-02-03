package dev.zagirnur.petbot.sdk.provider;

import java.util.Locale;

public interface BotI18n {
    String translate(String text, Locale locale);
}
