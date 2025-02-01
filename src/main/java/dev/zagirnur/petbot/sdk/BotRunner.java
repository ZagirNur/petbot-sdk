package dev.zagirnur.petbot.sdk;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@RequiredArgsConstructor
public class BotRunner implements CommandLineRunner {

    private static final Logger log = getLogger(BotRunner.class);

    private final BotConfigurer botConfigurer;

    @Override
    public void run(String... args) throws Exception {
        // Блокируем поток, чтобы приложение не завершалось
        synchronized (this) {
            try {
                for (BotConfigurer.RegisteredBot registeredBot : botConfigurer.getRegisteredBots()) {
                    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                    telegramBotsApi.registerBot(registeredBot.bot());
                    log.info("Bot {} started", registeredBot.botUsername());
                }
            } catch (TelegramApiException e) {
                log.error("Failed to start bot: ", e);
            }
            wait();
        }
    }
}