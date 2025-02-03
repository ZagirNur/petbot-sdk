package dev.zagirnur.petbot.sdk.provider;


import dev.zagirnur.petbot.sdk.annotations.OnCallback;
import dev.zagirnur.petbot.sdk.annotations.OnMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.annotation.Annotation;

import static org.apache.commons.lang3.StringUtils.isNoneEmpty;

@Component
public class UnprefixedStringUpdateDataProvider implements UpdateDataProvider {

    ContextProvider contextProvider;

    @Override
    public StringUpdateData getUpdateData(Annotation annotation, Update update) {
        if (annotation instanceof OnCallback) {
            String prefix = ((OnCallback) annotation).prefix();
            if (prefix == null) {
                prefix = "";
            }
            String data = update.getCallbackQuery().getData()
                    .replace(prefix, "");
            return StringUpdateData.of(data);
        }
        if (annotation instanceof OnMessage) {
            String state = ((OnMessage) annotation).state();
            if (isNoneEmpty(state)) {
                String data = contextProvider.getContext(update).getState()
                        .replace(state, "");
                return StringUpdateData.of(data);
            }
        }
        return StringUpdateData.of("");
    }

    @Override
    public String preSendMessage(String callbackData) {
        return callbackData;
    }
}
