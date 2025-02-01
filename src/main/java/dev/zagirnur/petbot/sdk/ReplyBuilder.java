package dev.zagirnur.petbot.sdk;

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

import static dev.zagirnur.petbot.sdk.UpdatePreProcessor.MESSSAGE_FOR_DELETE;

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
                .chatId(getALong().toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        long messageId = executeAndGetMessageId(sendMessage);
        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSSAGE_FOR_DELETE, messageId);
        }
    }

    private Long getALong() {
        return extractChatId(update);
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
                .chatId(extractChatId(update).toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSSAGE_FOR_DELETE, messageId.longValue());
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
        Long chatId = extractChatId(update);

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .build();

        execute(editMessage);

        if (tagDeleteAfterUpdateMessage) {
            context.tagMessageId(MESSSAGE_FOR_DELETE,
                    update.getCallbackQuery().getMessage().getMessageId().longValue());
        }
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return 0L;
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
                .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                .messageId(messageIdByTag.intValue())
                .build();

        execute(method);
    }
}