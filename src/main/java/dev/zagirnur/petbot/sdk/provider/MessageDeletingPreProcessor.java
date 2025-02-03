package dev.zagirnur.petbot.sdk.provider;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.zagirnur.petbot.sdk.util.BotUtils.getChatId;

@RequiredArgsConstructor
public class MessageDeletingPreProcessor implements UpdatePrePostProcessor {

    private final ContextProvider contextProvider;

    public static final String MESSAGE_FOR_DELETE = "MESSAGE_FOR_DELETE";

    @Override
    public void preProcess(Update update,
                           DefaultAbsSender sender) {
        ChatContext context = contextProvider.getContext(update);
        Long messageIdByTag = context.getMessageIdByTag(MESSAGE_FOR_DELETE);
        if (messageIdByTag != null) {
            context.deleteTag(MESSAGE_FOR_DELETE);
            contextProvider.saveContext(update, context);
            try {
                sender.execute(DeleteMessage.builder()
                        .chatId(getChatId(update))
                        .messageId(messageIdByTag.intValue())
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postProcess(Update update,
                            DefaultAbsSender sender) {
        // do nothing
    }
}