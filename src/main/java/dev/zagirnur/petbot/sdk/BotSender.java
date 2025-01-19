package dev.zagirnur.petbot.sdk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Класс-бин, который предоставляет удобные методы для отправки сообщений.
 */
@Component
@RequiredArgsConstructor
public class BotSender {

    private final TelegramBotFacade botFacade;
    private final UpdateDataProvider updateDataProvider;

    /**
     * Возвращает билдер для ответа на указанный update.
     */
    public ReplyBuilder reply(Update update) {
        // Можно внутри вернуть ваш "ReplyBuilder", которому в конструктор
        // передаём, собственно, бота (botFacade) и сам Update.
        return new ReplyBuilder(botFacade, update, updateDataProvider);
    }

    public void sendAnswerInlineQuery(String inlineQueryId, List<InlineQueryResult> results) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQueryId);
        answerInlineQuery.setResults(results);
        try {
            botFacade.execute(answerInlineQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}