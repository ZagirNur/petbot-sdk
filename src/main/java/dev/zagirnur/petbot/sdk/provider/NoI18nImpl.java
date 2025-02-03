package dev.zagirnur.petbot.sdk.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.Locale;

@ConditionalOnMissingBean(BotI18n.class)
public class NoI18nImpl implements BotI18n {
    @Override
    public String translate(String text, Locale locale) {
        return text;
    }
}
