package dev.zagirnur.petbot.sdk;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Подключает зарегистрированных ботов к Telegram, когда приложение готово.
 * <p>
 * Опрос обновлений ведёт собственный поток внутри {@link DefaultBotSession}, поэтому
 * поток запуска блокировать не нужно: раньше здесь стоял бесконечный {@code wait()},
 * из-за которого приложение нельзя было поднять в тесте. Старт по
 * {@link ApplicationReadyEvent}, а не при создании бина, чтобы первые сообщения
 * не пришли раньше, чем контекст полностью готов.
 */
@Component
@RequiredArgsConstructor
public class BotRunner {

    private static final Logger log = getLogger(BotRunner.class);

    private final BotConfigurer botConfigurer;

    @EventListener(ApplicationReadyEvent.class)
    public void startBots() {
        for (BotConfigurer.RegisteredBot registeredBot : botConfigurer.getRegisteredBots()) {
            try {
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                telegramBotsApi.registerBot(registeredBot.bot());
                log.info("Bot {} started", registeredBot.botUsername());
            } catch (TelegramApiException e) {
                log.error("Failed to start bot {}: ", registeredBot.botUsername(), e);
            }
        }
    }
}
