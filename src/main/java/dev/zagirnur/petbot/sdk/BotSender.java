package dev.zagirnur.petbot.sdk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Locale;

/**
 * Класс-бин, который предоставляет удобные методы для отправки сообщений.
 */
@Component
@RequiredArgsConstructor
public class BotSender {

    private final TelegramBotFacade botFacade;
    private final UpdateDataProvider updateDataProvider;
    private final BotI18n i18n;

    /**
     * Возвращает билдер для ответа на указанный update.
     */
    public ReplyBuilder reply(Update update) {
        return new ReplyBuilder(botFacade, update, updateDataProvider, i18n, Locale.ENGLISH);
    }

    public ReplyBuilder reply(Update update, Locale locale) {
        return new ReplyBuilder(botFacade, update, updateDataProvider, i18n, locale);
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

    public class I18nTableBuilder extends TableBuilder {
        private Locale locale =Locale.ENGLISH;

        public I18nTableBuilder(List<String> header, Character headerSeparator, String columnSeparator) {
            super(header, headerSeparator, columnSeparator);
        }

        public I18nTableBuilder(Character headerSeparator, String columnSeparator) {
            super(headerSeparator, columnSeparator);
        }

        public I18nTableBuilder(String columnSeparator) {
            super(columnSeparator);
        }

        public I18nTableBuilder() {
            super();
        }

        public I18nTableBuilder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        @Override
        public String build() {
            var l =  super.header.stream()
                    .map(h -> i18n.translate(h, locale))
                    .toList();
            super.header = l;
            return super.build();
        }
    }


}