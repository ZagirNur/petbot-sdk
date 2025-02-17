package dev.zagirnur.petbot.sdk;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class SenderHolderBotProcessor {

    private final ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private final ConcurrentHashMap<Update, String> updateCache = new ConcurrentHashMap<>();

    private final Map<String, BotConfigurer.SenderHolder> botCache = new ConcurrentHashMap<>();

    @Lazy
    @Autowired
    private BotConfigurer botConfigurer;

    public void preProcess(Update update,
                           String botName) {
        threadLocal.set(botName);
        updateCache.put(update, botName);
    }

    public void postProcess(Update update) {
        threadLocal.remove();
        updateCache.remove(update);
    }

    public BotConfigurer.SenderHolder getBot(Update update) {
        BotConfigurer.SenderHolder registeredBot = null;
        if (botCache.size() == 1) {
            return botCache.values().iterator().next();
        }
        if (update != null) {
            registeredBot = botCache.get(updateCache.get(update));
        }
        if (registeredBot == null) {
            registeredBot = botCache.get(threadLocal.get());
        }
        if (registeredBot == null) {
            throw new RuntimeException("Cannot find bot sender for session");
        }
        return registeredBot;
    }

}