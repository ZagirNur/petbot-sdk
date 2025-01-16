package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnInlineQuery;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class TelegramBotFacade extends TelegramLongPollingBot {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotFacade.class);

    private final TelegramBotProperties properties; // Для токена и имени
    private final HandlerRegistry handlerRegistry;  // Для зарегистрированных обработчиков
    private final ContextProvider contextProvider;
    @Lazy
    @Autowired
    private UpdatePreProcessor updatePreProcessor;
    @Lazy
    @Autowired
    private ExceptionHandler exceptionHandler;

    public TelegramBotFacade(TelegramBotProperties properties,
                             HandlerRegistry handlerRegistry,
                             ContextProvider contextProvider
    ) {
        this.properties = properties;
        this.handlerRegistry = handlerRegistry;
        this.contextProvider = contextProvider;
    }

    @Override
    public String getBotToken() {
        return properties.getToken(); // Токен из пропертей
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername(); // Имя из пропертей
    }

    @Override
    public void onUpdateReceived(Update update) {

        log.info("Received update: {}", update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            handleIncomingMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleIncomingCallback(update);
        } else if (update.hasInlineQuery()) {
            handleIncomingInlineQuery(update);
        }
    }

    private void handleIncomingMessage(Update update) {
        updatePreProcessor.preProcess(update, this);

        String text = update.getMessage().getText();

        String state = contextProvider.getContext(update).getState();
        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getMessageHandlers()) {
            Method method = handler.getMethod();
            OnMessage annotation = method.getAnnotation(OnMessage.class);

            boolean notMatchedCommand = !annotation.command().isEmpty() && !text.equals(annotation.command());
            if (notMatchedCommand) continue;
            boolean notMatchedPrefix = !annotation.prefix().isEmpty() && !text.startsWith(annotation.prefix());
            if (notMatchedPrefix) continue;
            boolean notMatchedState = !annotation.state().isEmpty() && !state.startsWith(annotation.state());
            if (notMatchedState) continue;
            boolean notMatchedRegexp = !annotation.regexp().isEmpty() && !text.matches(annotation.regexp());
            if (notMatchedRegexp) continue;

            invokeHandlerMethod(handler, update);
            break;
        }
    }

    private void handleIncomingCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();

        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getCallbackHandlers()) {
            Method method = handler.getMethod();
            OnCallback annotation = method.getAnnotation(OnCallback.class);

            if (callbackData.startsWith(annotation.prefix())) {
                invokeHandlerMethod(handler, update); // Вызов метода
                break;
            }
        }
    }

    private void handleIncomingInlineQuery(Update update) {
        String inlaineQuery = update.getInlineQuery().getQuery();

        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getInlineQueryHandlers()) {
            Method method = handler.getMethod();
            OnInlineQuery annotation = method.getAnnotation(OnInlineQuery.class);

            if (annotation.prefix().isEmpty() || inlaineQuery.startsWith(annotation.prefix())) {
                invokeHandlerMethod(handler, update); // Вызов метода
                break;
            }
        }
    }

    private void invokeHandlerMethod(HandlerRegistry.HandlerMethod handler, Update update) {
        try {
            Method method = handler.getMethod();
            Object[] parameters = resolveParameters(method, update);

            // Извлекаем ChatContext, если он используется
            ChatContext chatContext = null;
            for (Object param : parameters) {
                if (param instanceof ChatContext) {
                    chatContext = (ChatContext) param;
                    break;
                }
            }

            // Вызываем метод обработчика
            method.invoke(handler.getBean(), parameters);

            // Сохраняем контекст, если он был изменён
            if (chatContext != null) {
                contextProvider.saveContext(update, chatContext);
            }
        } catch (InvocationTargetException e) {
            exceptionHandler.handle(update, e.getTargetException());
        } catch (Throwable e) {
            exceptionHandler.handle(update, e);
        }
    }

    private Object[] resolveParameters(Method method, Update update) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(Update.class)) {
                parameters[i] = update; // Передаём сам Update
            } else if (ChatContext.class.isAssignableFrom(parameterTypes[i])) {
                // Достаём Stateful из контекста
                parameters[i] = contextProvider.getContext(update);
            } else {
                parameters[i] = null;
            }
        }

        return parameters;
    }
}