package dev.zagirnur.petbot.sdk.provider;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.annotation.Annotation;

public interface UpdateDataProvider {

    UpdateData getUpdateData(Annotation annotation, Update update);

    String preSendMessage(String callbackData);

}
