package dev.zagirnur.petbot.sdk.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.ShippingQuery;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;

import java.util.Optional;

@UtilityClass
public class BotUtils {

    @SuppressWarnings("DuplicatedCode")
    public static User getFrom(Update update) {
        Optional<User> from = Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getInlineQuery)
                .map(InlineQuery::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getChosenInlineQuery)
                .map(ChosenInlineQuery::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getShippingQuery)
                .map(ShippingQuery::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getPreCheckoutQuery)
                .map(PreCheckoutQuery::getFrom);
        if (from.isPresent()) return from.get();

        from = Optional.ofNullable(update)
                .map(Update::getPollAnswer)
                .map(PollAnswer::getUser);
        return from.orElse(null);

    }

    public static Long getMessageId(Update update) {
        Optional<Long> messageId = Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getMessageId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getMessage)
                .map(MaybeInaccessibleMessage::getMessageId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getChosenInlineQuery)
                .map(ChosenInlineQuery::getResultId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getInlineQuery)
                .map(InlineQuery::getId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getShippingQuery)
                .map(ShippingQuery::getId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getPreCheckoutQuery)
                .map(PreCheckoutQuery::getId)
                .map(Long::valueOf);
        if (messageId.isPresent()) return messageId.get();

        messageId = Optional.ofNullable(update)
                .map(Update::getPollAnswer)
                .map(PollAnswer::getPollId)
                .map(Long::valueOf);
        return messageId.orElse(null);
    }

    public static String getChatId(Update update) {
        Optional<String> chatId = Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getChatId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getMessage)
                .map(MaybeInaccessibleMessage::getChatId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getChosenInlineQuery)
                .map(ChosenInlineQuery::getFrom)
                .map(User::getId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getInlineQuery)
                .map(InlineQuery::getFrom)
                .map(User::getId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getShippingQuery)
                .map(ShippingQuery::getFrom)
                .map(User::getId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getPreCheckoutQuery)
                .map(PreCheckoutQuery::getFrom)
                .map(User::getId)
                .map(String::valueOf);
        if (chatId.isPresent()) return chatId.get();

        chatId = Optional.ofNullable(update)
                .map(Update::getPollAnswer)
                .map(PollAnswer::getUser)
                .map(User::getId)
                .map(String::valueOf);
        return chatId.orElse(null);
    }
}
