package dev.zagirnur.petbot.sdk;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class HandlerRegistry {

    private final List<HandlerMethod> messageHandlers = new ArrayList<>();
    private final List<HandlerMethod> callbackHandlers = new ArrayList<>();
    private final List<HandlerMethod> inlineQueryHandlers = new ArrayList<>();
    private List<Class> handlerClassesOrder = new ArrayList<>();
    private boolean isOrderSet = false;
    private boolean otherHandlersLast = true;

    public final void setHandlersOrder(List<Class> handlerClasses, boolean otherHandlersLast) {
        this.handlerClassesOrder = new ArrayList<>(handlerClasses); // Копируем список
        this.isOrderSet = true;
        this.otherHandlersLast = otherHandlersLast;

        // Пересортируем уже добавленные обработчики
        sortHandlers(messageHandlers);
        sortHandlers(callbackHandlers);
        sortHandlers(inlineQueryHandlers);
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
        // Сортировка по handlerClassesOrder
        handlers.sort(Comparator.comparingInt(h -> {
            int index = handlerClassesOrder.indexOf(h.getBean().getClass());
            return index >= 0 ? index :
                    (otherHandlersLast ? handlerClassesOrder.size() : Integer.MIN_VALUE); // Если не найдено, то в конец
        }));
    }

    public List<HandlerMethod> getMessageHandlers() {
        return messageHandlers;
    }

    public List<HandlerMethod> getCallbackHandlers() {
        return callbackHandlers;
    }

    public List<HandlerMethod> getInlineQueryHandlers() {
        return inlineQueryHandlers;
    }

    public static class HandlerMethod {
        private final Object bean;
        private final Method method;

        public HandlerMethod(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }

        public Object getBean() {
            return bean;
        }

        public Method getMethod() {
            return method;
        }
    }
}