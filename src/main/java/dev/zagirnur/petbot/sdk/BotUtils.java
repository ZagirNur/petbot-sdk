package dev.zagirnur.petbot.sdk;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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
        if (from.isPresent()) return from.get();

        throw new IllegalArgumentException("Can't get chatId from update");
    }
}
