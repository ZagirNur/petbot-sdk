package dev.zagirnur.petbot.sdk;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class SenderHolderBotProcessor {

    private final ThreadLocal<BotConfigurer.SenderHolder> absSender = new ThreadLocal<>();
    private final ConcurrentHashMap<Update, BotConfigurer.SenderHolder> cache = new ConcurrentHashMap<>();

    @Lazy
    @Autowired
    private BotConfigurer botConfigurer;

    public void preProcess(Update update,
                           BotConfigurer.SenderHolder bot) {
        absSender.set(bot);
        cache.put(update, bot);
    }

    public void postProcess(Update update) {
        absSender.remove();
        cache.remove(update);
    }

    public BotConfigurer.SenderHolder getBot(Update update) {
        BotConfigurer.SenderHolder registeredBot = cache.get(update);
        if (registeredBot == null) {
            registeredBot = absSender.get();
        }
        return registeredBot;
    }

}