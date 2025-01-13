package dev.zagirnur.petbot.sdk;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Упрощённый билдер для формирования ответа (send или update).
 */
public class ReplyBuilder {

    private final TelegramBotFacade bot;
    private final Update update;

    private String text;
    private InlineKeyboardMarkup keyboard;

    public ReplyBuilder(TelegramBotFacade bot, Update update) {
        this.bot = bot;
        this.update = update;
    }

    public ReplyBuilder text(String text) {
        this.text = text;
        return this;
    }

    public ReplyBuilder inlineKeyboard(List<InlineKeyboardButton>... rows) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(rows));
        this.keyboard = inlineKeyboardMarkup;
        return this;
    }

    public static List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return List.of(buttons);
    }

    public static List<InlineKeyboardButton> row(String text, String callbackData) {
        return row(btn(text, callbackData));
    }


    public static InlineKeyboardButton btn(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    public static InlineKeyboardButton btnSwitch(String text, String switchInlineQuery) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setSwitchInlineQuery(switchInlineQuery);
        return inlineKeyboardButton;
    }

    /**
     * Отправляем новое сообщение (SendMessage).
     */
    public void send() {
        Long chatId = extractChatId(update);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);

        if (keyboard != null) {
            sendMessage.setReplyMarkup(keyboard);
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновляем существующее сообщение (EditMessageText).
     */
    public void update(Integer messageId) {
        Long chatId = extractChatId(update);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setText(text);

        if (keyboard != null) {
            editMessage.setReplyMarkup(keyboard);
        }

        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
        AnswerCallbackQuery build = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(text)
                .showAlert(true)
                .build();

        try {
            bot.execute(build);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }


}