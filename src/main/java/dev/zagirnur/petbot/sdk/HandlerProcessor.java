package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnInlineQuery;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HandlerProcessor implements BeanPostProcessor {

    private final BotConfigurer botConfigurer;

    @Override

    public Object postProcessAfterInitialization(Object bean, @Nullable String beanName) {
        Class<?> beanClass = bean.getClass();
        Optional<BotConfigurer.RegisteredBot> first = botConfigurer.registeredBots.stream()
                .filter(registeredBot -> registeredBot.handlerClasses().contains(beanClass))
                .findFirst();
        if (first.isEmpty()) {
            return bean;
        }
        var handlerRegistry = first.get().handlerRegistry();
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                handlerRegistry.registerMessageHandler(bean, method);
            }
            if (method.isAnnotationPresent(OnCallback.class)) {
                handlerRegistry.registerCallbackHandler(bean, method);
            }
            if (method.isAnnotationPresent(OnInlineQuery.class)) {
                handlerRegistry.registerInlineQueryHandler(bean, method);
            }
        }

        return bean;
    }
}