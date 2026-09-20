package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.annotation.Annotation;

public interface UpdateDataProvider {

    UpdateData getUpdateData(Annotation annotation, Update update);

    String preSendMessage(String callbackData);

    /**
     * Разбирает данные апдейта в конкретный тип параметра обработчика.
     * <p>
     * Знание целевого типа позволяет реализации десериализовать callback_data напрямую,
     * не угадывая тип по префиксу кнопки. Реализация по умолчанию сохраняет прежнее
     * поведение и игнорирует тип.
     *
     * @return значение параметра либо {@code null}, если разобрать не удалось
     */
    default Object resolveParameter(Annotation annotation, Update update, Class<?> targetType) {
        return getUpdateData(annotation, update);
    }

}
