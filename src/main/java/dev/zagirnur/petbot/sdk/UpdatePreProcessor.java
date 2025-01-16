package dev.zagirnur.petbot.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class UpdatePreProcessor {

    @Autowired
    private ContextProvider contextProvider;

    public static final String MESSSAGE_FOR_DELETE = "MESSSAGE_FOR_DELETE";



    public void preProcess(Update update, DefaultAbsSender sender) {
        ChatContext context = contextProvider.getContext(update);
        Long messageIdByTag = context.getMessageIdByTag(MESSSAGE_FOR_DELETE);
        if (messageIdByTag != null) {
            context.deleteTag(MESSSAGE_FOR_DELETE);
            contextProvider.saveContext(update, context);
            try {
                sender.execute(DeleteMessage.builder()
                        .chatId(update.getMessage().getChatId().toString())
                        .messageId(messageIdByTag.intValue())
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}