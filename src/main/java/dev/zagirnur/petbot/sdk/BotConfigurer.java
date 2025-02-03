package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.provider.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class BotConfigurer {

    final List<RegisteredBot> registeredBots = new ArrayList<>();
    private final SenderHolderBotProcessor senderHolderBotProcessor;

    public RegisteredBot getBot(String botUsername) {
        return registeredBots.stream()
                .filter(bot -> bot.botUsername().equals(botUsername))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
    }

    @Builder
    public static class Bot {
        private String botUsername;
        private String botToken;
        private List<Class<?>> handlerClasses;
        private ContextProvider contextProvider;
        private UserProvider userProvider;
        private UpdateDataProvider updateDataProvider;
        @Singular
        private List<UpdatePrePostProcessor> updatePreProcessors;
        private ExceptionHandler exceptionHandler;
        private BotI18n i18n;
    }

    @SuppressWarnings("unused")
    public BotConfigurer addBot(Bot botBuilder) {
        if (botBuilder.botUsername == null) {
            throw new IllegalArgumentException("Bot username is required");
        }
        if (botBuilder.botToken == null) {
            throw new IllegalArgumentException("Bot token is required");
        }
        if (botBuilder.handlerClasses == null) {
            throw new IllegalArgumentException("Handler classes are required");
        }
        if (botBuilder.contextProvider == null) {
            botBuilder.contextProvider = new ContextProvider() {
                @Override
                public ChatContext getContext(Update update) {
                    return new ChatContext() {
                        @Override
                        public void deleteTag(String tag) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public String getState() {
                            return null;
                        }

                        @Override
                        public void setState(String state) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void cleanState() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Long getMessageIdByTag(String tag) {
                            return null;
                        }

                        @Override
                        public void tagMessageId(String tag,
                                                 Long messageId) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public void saveContext(Update update,
                                        ChatContext context) {
                }
            };
        }

        if (botBuilder.updateDataProvider == null) {
            botBuilder.updateDataProvider = new UpdateDataProvider() {

                @Override
                public UpdateData getUpdateData(Annotation annotation,
                                                Update update) {
                    return null;
                }

                @Override
                public String preSendMessage(String callbackData) {
                    return callbackData;
                }
            };
        }

        if (botBuilder.exceptionHandler == null) {
            botBuilder.exceptionHandler = new ExceptionHandler() {

                @Override
                public void handle(Update update,
                                   Throwable t) {
                    log.error("Error while processing update", t);
                }
            };
        }

        if (botBuilder.i18n == null) {
            botBuilder.i18n = new BotI18n() {
                @Override
                public String translate(String text,
                                        Locale locale) {
                    return text;
                }
            };
        }

        return addBot(
                botBuilder.botUsername,
                botBuilder.botToken,
                botBuilder.handlerClasses,
                botBuilder.contextProvider,
                botBuilder.updateDataProvider,
                botBuilder.updatePreProcessors,
                botBuilder.exceptionHandler,
                botBuilder.i18n
        );
    }

    @SuppressWarnings("unused")
    public BotConfigurer addBot(
            String botUsername,
            String botToken,
            List<Class<?>> handlerClasses,
            ContextProvider contextProvider,
            UpdateDataProvider updateDataProvider,
            List<UpdatePrePostProcessor> updatePreProcessors,
            ExceptionHandler exceptionHandler,
            BotI18n i18n
    ) {
        HandlerRegistry handlerRegistry = new HandlerRegistry()
                .withHandlers(handlerClasses);


        SenderHolder senderHolder = new SenderHolder(
                new DefaultAbsSender(new DefaultBotOptions(), botToken) {
                },
                i18n,
                updateDataProvider
        );

        TelegramBotFacade telegramBotFacade =
                new TelegramBotFacade(
                        botUsername,
                        botToken,
                        handlerRegistry,
                        contextProvider,
                        updateDataProvider,
                        updatePreProcessors,
                        exceptionHandler
                ) {
                    @Override
                    public void onUpdateReceived(Update update) {
                        senderHolderBotProcessor.preProcess(update, senderHolder);
                        super.onUpdateReceived(update);
                        senderHolderBotProcessor.postProcess(update);
                    }
                };


        RegisteredBot registered = new RegisteredBot(
                botUsername,
                botToken,
                telegramBotFacade,
                handlerClasses,
                handlerRegistry,
                updateDataProvider
        );


        registeredBots.add(registered);
        return this;
    }


    public record RegisteredBot(
            String botUsername,
            String botToken,
            TelegramBotFacade bot,
            List<Class<?>> handlerClasses,
            HandlerRegistry handlerRegistry,
            UpdateDataProvider updateDataProvider
    ) {
    }

    public record SenderHolder(
            AbsSender bot,
            BotI18n i18n,
            UpdateDataProvider updateDataProvider
    ) {
    }


}
