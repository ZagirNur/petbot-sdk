package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnInlineQuery;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HandlerProcessor implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final BotConfigurer botConfigurer;

    @Override
    public void afterSingletonsInstantiated() {
        botConfigurer.registeredBots.stream()
                .map(BotConfigurer.RegisteredBot::handlerClasses)
                .flatMap(List::stream)
                .collect(Collectors.toSet()).stream()
                .map(applicationContext::getBean)
                .forEach(this::postProcessAfterInitialization);
    }

    public void postProcessAfterInitialization(Object bean) {
        Class<?> beanClass = bean.getClass();
        Optional<BotConfigurer.RegisteredBot> first = botConfigurer.registeredBots.stream()
                .filter(registeredBot -> registeredBot.handlerClasses().contains(beanClass))
                .findFirst();
        if (first.isEmpty()) {
            return;
        }
        var handlerRegistry = first.get().handlerRegistry();
        for (Method method : beanClass.getDeclaredMethods()) {
            for (OnMessage onMessage : method.getAnnotationsByType(OnMessage.class)) {
                handlerRegistry.registerMessageHandler(bean, method, onMessage);
            }
            for (OnCallback onCallback : method.getAnnotationsByType(OnCallback.class)) {
                handlerRegistry.registerCallbackHandler(bean, method, onCallback);
            }
            for (OnInlineQuery onInlineQuery : method.getAnnotationsByType(OnInlineQuery.class)) {
                handlerRegistry.registerInlineQueryHandler(bean, method, onInlineQuery);
            }
        }
    }
}