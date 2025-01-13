package dev.zagirnur.petbot.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotRunner implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotRunner.class);

    @Autowired
    TelegramBotFacade telegramBotFacade;

    @Override
    public void run(String... args) throws Exception {
        // Блокируем поток, чтобы приложение не завершалось
        synchronized (this) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(telegramBotFacade);
                log.info("Bot started successfully!");
            } catch (TelegramApiException e) {
                log.error("Failed to start bot: ", e);
            }
            wait();
        }
    }
}