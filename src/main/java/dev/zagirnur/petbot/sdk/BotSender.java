package dev.zagirnur.petbot.sdk;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class BotSender {

    private final UpdateDataProvider updateDataProvider;
    private final BotI18n i18n;
    private final AbsSender absSender;

    public BotSender(
            String token,
            UpdateDataProvider updateDataProvider,
            BotI18n i18n) {
        this.updateDataProvider = updateDataProvider;
        this.i18n = i18n;
        this.absSender = new TelegramLongPollingBot(token) {
            @Override
            public String getBotUsername() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onUpdateReceived(Update update) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Возвращает билдер для ответа на указанный update.
     */
    public ReplyBuilder reply(Update update) {
        return new ReplyBuilder(absSender, update, updateDataProvider, i18n, Locale.ENGLISH);
    }

    public ReplyBuilder reply(Update update,
                              Locale locale) {
        return new ReplyBuilder(absSender, update, updateDataProvider, i18n, locale);
    }

    public void sendAnswerInlineQuery(String inlineQueryId,
                                      List<InlineQueryResult> results) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQueryId);
        answerInlineQuery.setResults(results);
        try {
            absSender.execute(answerInlineQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public class I18nTableBuilder extends TableBuilder {
        private Locale locale = Locale.ENGLISH;

        public I18nTableBuilder(List<String> header,
                                Character headerSeparator,
                                String columnSeparator) {
            super(header, headerSeparator, columnSeparator);
        }

        public I18nTableBuilder(Character headerSeparator,
                                String columnSeparator) {
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
            super.header = super.header.stream()
                    .map(h -> i18n.translate(h, locale))
                    .toList();
            return super.build();
        }
    }


}