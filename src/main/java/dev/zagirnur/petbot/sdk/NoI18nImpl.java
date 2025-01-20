package dev.zagirnur.petbot.sdk;

import org.jvnet.hk2.annotations.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.Locale;

@ConditionalOnMissingBean(BotI18n.class)
public class NoI18nImpl implements BotI18n {
    @Override
    public String translate(String text, Locale locale) {
        return text;
    }
}
