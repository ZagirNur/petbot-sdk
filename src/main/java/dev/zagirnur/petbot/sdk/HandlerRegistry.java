package dev.zagirnur.petbot.sdk;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class HandlerRegistry {

    @Getter
    private final List<HandlerMethod> messageHandlers = new ArrayList<>();
    @Getter
    private final List<HandlerMethod> callbackHandlers = new ArrayList<>();
    @Getter
    private final List<HandlerMethod> inlineQueryHandlers = new ArrayList<>();
    private List<Class<?>> handlerClassesOrder = new ArrayList<>();
    private boolean isOrderSet = false;

    public final HandlerRegistry withHandlers(List<Class<?>> handlerClasses) {
        this.handlerClassesOrder = new ArrayList<>(handlerClasses); // Копируем список
        this.isOrderSet = true;

        // Пересортируем уже добавленные обработчики
        sortHandlers(messageHandlers);
        sortHandlers(callbackHandlers);
        sortHandlers(inlineQueryHandlers);
        return this;
    }

    public void registerMessageHandler(Object bean, Method method) {
        messageHandlers.add(new HandlerMethod(bean, method));
        if (isOrderSet) {
            sortHandlers(messageHandlers);
        }
    }

    public void registerCallbackHandler(Object bean, Method method) {
        callbackHandlers.add(new HandlerMethod(bean, method));
        if (isOrderSet) {
            sortHandlers(callbackHandlers);
        }
    }

    public void registerInlineQueryHandler(Object bean, Method method) {
        inlineQueryHandlers.add(new HandlerMethod(bean, method));
        if (isOrderSet) {
            sortHandlers(inlineQueryHandlers);
        }

    }

    private void sortHandlers(List<HandlerMethod> handlers) {
        handlers.sort(Comparator.comparingInt(h -> {
            int index = handlerClassesOrder.indexOf(h.bean().getClass());
            if (index == -1) {
                throw new IllegalStateException("Handler class not found in order list: " + h.bean().getClass());
            }
            return index;
        }));
    }

    public record HandlerMethod(Object bean, Method method) {
    }
}