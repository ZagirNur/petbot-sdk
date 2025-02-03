package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnInlineQuery;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import dev.zagirnur.petbot.sdk.provider.*;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TelegramBotFacade extends TelegramLongPollingBot {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotFacade.class);

    private final String botUsername;
    @Getter
    private final HandlerRegistry handlerRegistry;  // Для зарегистрированных обработчиков
    private final ContextProvider contextProvider;
    private final UpdateDataProvider updateDataProvider;
    private final List<UpdatePrePostProcessor> updatePreProcessors;
    private final ExceptionHandler exceptionHandler;

    public TelegramBotFacade(
            String botUsername,
            String botToken,
            HandlerRegistry handlerRegistry,
            ContextProvider contextProvider,
            UpdateDataProvider updateDataProvider,
            List<UpdatePrePostProcessor> updatePreProcessors,
            ExceptionHandler exceptionHandler
    ) {
        super(botToken);
        this.botUsername = botUsername;
        this.handlerRegistry = handlerRegistry;
        this.contextProvider = contextProvider;
        this.updateDataProvider = updateDataProvider;
        this.updatePreProcessors = updatePreProcessors;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {

        log.info("Received update: {}", update);

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleIncomingMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleIncomingCallback(update);
            } else if (update.hasInlineQuery()) {
                handleIncomingInlineQuery(update);
            }
        } catch (InvocationTargetException e) {
            exceptionHandler.handle(update, e.getTargetException());
        } catch (Throwable e) {
            exceptionHandler.handle(update, e);
        }
    }

    private void handleIncomingMessage(Update update) throws InvocationTargetException, IllegalAccessException {

        updatePreProcessors.forEach(updatePreProcessor ->
                updatePreProcessor.preProcess(update, this));

        String text = update.getMessage().getText();

        String state = contextProvider.getContext(update).getState();
        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getMessageHandlers()) {
            Method method = handler.method();
            OnMessage annotation = method.getAnnotation(OnMessage.class);

            boolean notMatchedCommand = !annotation.command().isEmpty() && !text.equals(annotation.command());
            if (notMatchedCommand) {
                continue;
            }
            boolean notMatchedPrefix = !annotation.prefix().isEmpty() && !text.startsWith(annotation.prefix());
            if (notMatchedPrefix) {
                continue;
            }
            boolean notMatchedState = !annotation.state().isEmpty() && !state.startsWith(annotation.state());
            if (notMatchedState) {
                continue;
            }
            boolean notMatchedRegexp = !annotation.regexp().isEmpty() && !text.matches(annotation.regexp());
            if (notMatchedRegexp) {
                continue;
            }

            invokeHandlerMethod(annotation, handler, update);
            break;
        }
    }

    private void handleIncomingCallback(Update update) throws InvocationTargetException, IllegalAccessException {
        String callbackData = update.getCallbackQuery().getData();

        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getCallbackHandlers()) {
            Method method = handler.method();
            OnCallback annotation = method.getAnnotation(OnCallback.class);

            if (callbackData.startsWith(annotation.prefix())) {
                invokeHandlerMethod(annotation, handler, update); // Вызов метода
                break;
            }
        }
    }

    private void handleIncomingInlineQuery(Update update) throws InvocationTargetException, IllegalAccessException {
        String inlineQuery = update.getInlineQuery().getQuery();

        for (HandlerRegistry.HandlerMethod handler : handlerRegistry.getInlineQueryHandlers()) {
            Method method = handler.method();
            OnInlineQuery annotation = method.getAnnotation(OnInlineQuery.class);

            if (annotation.prefix().isEmpty() || inlineQuery.startsWith(annotation.prefix())) {
                invokeHandlerMethod(annotation, handler, update); // Вызов метода
                break;
            }
        }
    }

    private void invokeHandlerMethod(Annotation annotation,
                                     HandlerRegistry.HandlerMethod handler,
                                     Update update) throws InvocationTargetException, IllegalAccessException {
        Method method = handler.method();
        Object[] parameters = resolveParameters(annotation, method, update);

        // Извлекаем ChatContext, если он используется
        ChatContext chatContext = null;
        for (Object param : parameters) {
            if (param instanceof ChatContext) {
                chatContext = (ChatContext) param;
                break;
            }
        }

        // Вызываем метод обработчика
        method.invoke(handler.bean(), parameters);

        // Сохраняем контекст, если он был изменён
        if (chatContext != null) {
            contextProvider.saveContext(update, chatContext);
        }
    }

    private Object[] resolveParameters(Annotation annotation,
                                       Method method,
                                       Update update) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(Update.class)) {
                parameters[i] = update; // Передаём сам Update
            } else if (ChatContext.class.isAssignableFrom(parameterTypes[i])) {
                parameters[i] = contextProvider.getContext(update);
            } else if (UpdateData.class.isAssignableFrom(parameterTypes[i])) {
                parameters[i] = updateDataProvider.getUpdateData(annotation, update);
            } else {
                parameters[i] = null;
            }
        }

        return parameters;
    }
}