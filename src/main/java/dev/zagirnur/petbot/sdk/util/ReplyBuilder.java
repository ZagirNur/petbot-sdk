package dev.zagirnur.petbot.sdk.util;

import dev.zagirnur.petbot.sdk.provider.BotI18n;
import dev.zagirnur.petbot.sdk.provider.ChatContext;
import dev.zagirnur.petbot.sdk.provider.UpdateDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static dev.zagirnur.petbot.sdk.util.BotUtils.getChatId;
import static dev.zagirnur.petbot.sdk.util.BotUtils.getMessageId;
import static dev.zagirnur.petbot.sdk.provider.MessageDeletingPreProcessor.MESSAGE_FOR_DELETE;

/**
 * Упрощённый билдер для формирования ответа (send или update).
 */
@Slf4j
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ReplyBuilder {

    private final AbsSender bot;
    private final Update update;
    private final UpdateDataProvider updateDataProvider;
    private final BotI18n i18n;
    private final Locale locale;

    private String text;
    private InlineKeyboardMarkup keyboard;
    private boolean tagDeleteAfterUpdateMessage = false;
    private ChatContext context;


    public ReplyBuilder text(String text) {
        this.text = i18n.translate(text, locale);
        return this;
    }

    public ReplyBuilder deleteAfterUpdateMessage(ChatContext context) {
        this.tagDeleteAfterUpdateMessage = true;
        this.context = context;
        return this;
    }

    @SafeVarargs
    public final ReplyBuilder inlineKeyboard(List<InlineKeyboardButton>... rows) {
        List<List<InlineKeyboardButton>> rowsList = Arrays.stream(rows)
                .map(r -> r.stream()
                        .peek(btn -> {
                            if (btn.getCallbackData() != null) {
                                btn.setCallbackData(updateDataProvider.preSendMessage(btn.getCallbackData()));
                            }
                            btn.setText(i18n.translate(btn.getText(), locale));
                        }).toList())
                .toList();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(rows));

        this.keyboard = inlineKeyboardMarkup;
        return this;
    }

    public static List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return List.of(buttons);
    }

    public static List<InlineKeyboardButton> row(String text,
                                                 String callbackData) {
        return row(btn(text, callbackData));
    }


    public static InlineKeyboardButton btn(String text,
                                           String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    public static InlineKeyboardButton btnSwitch(String text,
                                                 String switchInlineQuery) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setSwitchInlineQuery(switchInlineQuery);
        return inlineKeyboardButton;
    }

    /**
     * Отправляем новое сообщение (SendMessage).
     */
    public void send() {

        SendMessage sendMessage = SendMessage.builder()
                .chatId(getChatId(update))
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        long messageId = executeAndGetMessageId(sendMessage);
        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSAGE_FOR_DELETE, messageId);
        }
    }

    private long executeAndGetMessageId(SendMessage sendMessage) {
        long messageId;
        try {
            messageId = bot.execute(sendMessage)
                    .getMessageId().longValue();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return messageId;
    }

    private void execute(BotApiMethod<?> method) {
        long messageId;
        try {
            bot.execute(method);
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse().contains("message is not modified")) {
                log.info("Message is not modified {}", method);
            } else {
                throw new RuntimeException(e);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Обновляем существующее сообщение (EditMessageText).
     */
    public void editMessage(Integer messageId) {

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(getChatId(update))
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSAGE_FOR_DELETE, messageId.longValue());
        }
    }

    public void editIfCallbackMessageOrSend() {
        if (update.hasCallbackQuery()) {
            try {
                editCallbackMessage();
            } catch (Exception e) {
                log.error("Error while editing message", e);
                send();
            }
        } else {
            send();
        }
    }

    public void editCallbackMessage() {

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(getChatId(update))
                .messageId(getMessageId(update).intValue())
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSAGE_FOR_DELETE, getMessageId(update));
        }
    }

    public void sendPopup() {
        AnswerCallbackQuery method = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(text)
                .showAlert(true)
                .build();

        execute(method);
    }


    public void deleteMessage(Long messageIdByTag) {
        DeleteMessage method = DeleteMessage.builder()
                .chatId(getChatId(update))
                .messageId(messageIdByTag.intValue())
                .build();

        execute(method);
    }
}