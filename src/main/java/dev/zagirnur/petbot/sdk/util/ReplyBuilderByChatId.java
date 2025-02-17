package dev.zagirnur.petbot.sdk.util;

import dev.zagirnur.petbot.sdk.provider.BotI18n;
import dev.zagirnur.petbot.sdk.provider.ChatContext;
import dev.zagirnur.petbot.sdk.provider.UpdateDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Упрощённый билдер для формирования ответа (send или update).
 */
@Slf4j
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ReplyBuilderByChatId {

    private final AbsSender bot;
    private final UpdateDataProvider updateDataProvider;
    private final BotI18n i18n;
    private final Locale locale;
    private final Long chatId;

    private Long messageId;
    private String text;
    private InlineKeyboardMarkup keyboard;
    private final List<Consumer<Long>> doWithSentMessageId = new ArrayList<>();
    private ChatContext context;

    public ReplyBuilderByChatId messageId(Long messageId) {
        this.messageId = messageId;
        return this;
    }

    public ReplyBuilderByChatId text(String text) {
        this.text = i18n.translate(text, locale);
        return this;
    }

    public ReplyBuilderByChatId doAfterMessageSent(Consumer<Long> consumer) {
        doWithSentMessageId.add(consumer);
        return this;
    }

    @SafeVarargs
    public final ReplyBuilderByChatId inlineKeyboard(List<InlineKeyboardButton>... rows) {
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
        return ReplyBuilder.btn(text, callbackData);
    }

    public static InlineKeyboardButton btnSwitch(String text,
                                                 String switchInlineQuery) {
        return ReplyBuilder.btnSwitch(text, switchInlineQuery);
    }

    /**
     * Отправляем новое сообщение (SendMessage).
     */
    public void send() {

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        long messageId = executeAndGetMessageId(sendMessage);
        doWithSentMessageId.forEach(consumer -> consumer.accept(messageId));
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
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        doWithSentMessageId.forEach(consumer -> consumer.accept((long) messageId));
    }

    public void editOrSend() {
        try {
            editCallbackMessage();
        } catch (Exception e) {
            send();
        }
    }

    public void editCallbackMessage() {

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId.intValue())
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        doWithSentMessageId.forEach(consumer ->
                consumer.accept(editMessage.getMessageId().longValue()));
    }

    public void deleteMessage(Long messageIdByTag) {
        DeleteMessage method = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageIdByTag.intValue())
                .build();

        execute(method);
    }

    public void deleteMessageIfExists(Long messageIdByTag) {
        try {
            deleteMessage(messageIdByTag);
        } catch (Exception ignore) {
            // ignore
        }
    }
}