package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnInlineQuery;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class HandlerProcessor implements BeanPostProcessor {

    private final HandlerRegistry handlerRegistry;

    public HandlerProcessor(HandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();

        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                handlerRegistry.registerMessageHandler(bean, method);
            } else if (method.isAnnotationPresent(OnCallback.class)) {
                handlerRegistry.registerCallbackHandler(bean, method);
            } else if (method.isAnnotationPresent(OnInlineQuery.class)) {
                handlerRegistry.registerInlineQueryHandler(bean, method);
            }
        }

        return bean;
    }
}