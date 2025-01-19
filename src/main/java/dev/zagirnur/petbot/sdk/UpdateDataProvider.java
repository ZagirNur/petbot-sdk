package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.annotations.TriggerAnnotation;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.annotation.Annotation;

public interface UpdateDataProvider {

    UpdateData getUpdateData(Annotation annotation, Update update);

    String preSendMessage(String callbackData);

}
