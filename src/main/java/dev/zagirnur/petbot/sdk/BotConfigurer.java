package dev.zagirnur.petbot.sdk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@RequiredArgsConstructor
public class BotConfigurer {

    List<RegisteredBot> registeredBots = new ArrayList<>();

    @SuppressWarnings("unused")
    public BotConfigurer addBot(
            String botUsername,
            String botToken,
            List<Class<?>> handlerClasses,
            ContextProvider contextProvider,
            UpdateDataProvider updateDataProvider,
            UpdatePreProcessor updatePreProcessor,
            ExceptionHandler exceptionHandler
    ) {
        TelegramBotFacade telegramBotFacade =
                new TelegramBotFacade(
                        botUsername,
                        botToken,
                        new HandlerRegistry()
                                .withHandlers(handlerClasses),
                        contextProvider,
                        updateDataProvider,
                        updatePreProcessor,
                        exceptionHandler);

        RegisteredBot registered = new RegisteredBot(
                botUsername,
                botToken,
                telegramBotFacade,
                handlerClasses,
                telegramBotFacade.getHandlerRegistry()
        );


        registeredBots.add(registered);
        return this;
    }


    record RegisteredBot(
            String botUsername,
            String botToken,
            TelegramLongPollingBot bot,
            List<Class<?>> handlerClasses,
            HandlerRegistry handlerRegistry) {
    }


}
