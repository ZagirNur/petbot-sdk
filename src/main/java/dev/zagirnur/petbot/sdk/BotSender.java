package dev.zagirnur.petbot.sdk;

import dev.zagirnur.petbot.sdk.util.ReplyBuilder;
import dev.zagirnur.petbot.sdk.util.ReplyBuilderByChatId;
import dev.zagirnur.petbot.sdk.util.TableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Locale;


@SuppressWarnings("unused")
@Component
@RequiredArgsConstructor
public class BotSender {

    private final SenderHolderBotProcessor senderHolderBotProcessor;

    /**
     * Возвращает билдер для ответа на указанный update.
     */
    public ReplyBuilder reply(Update update) {
        BotConfigurer.SenderHolder bot = senderHolderBotProcessor.getBot(update);
        return new ReplyBuilder(bot.bot(), update, bot.updateDataProvider(), bot.i18n(), Locale.ENGLISH);
    }

    public ReplyBuilder reply(Update update,
                              Locale locale) {
        BotConfigurer.SenderHolder bot = senderHolderBotProcessor.getBot(update);
        return new ReplyBuilder(bot.bot(), update, bot.updateDataProvider(), bot.i18n(), locale);
    }

    public ReplyBuilderByChatId reply(Long chatId) {
        BotConfigurer.SenderHolder bot = senderHolderBotProcessor.getBot(null);
        return new ReplyBuilderByChatId(bot.bot(), bot.updateDataProvider(), bot.i18n(), Locale.ENGLISH, chatId);
    }

    public ReplyBuilderByChatId reply(Long chatId,
                                      Locale locale) {
        BotConfigurer.SenderHolder bot = senderHolderBotProcessor.getBot(null);
        return new ReplyBuilderByChatId(bot.bot(), bot.updateDataProvider(), bot.i18n(), locale, chatId);
    }

    public void sendAnswerInlineQuery(String inlineQueryId,
                                      List<InlineQueryResult> results) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQueryId);
        BotConfigurer.SenderHolder bot = senderHolderBotProcessor.getBot(null);
        var translatedArticles = results.stream()
                .map(article -> {
                    if (article instanceof InlineQueryResultArticle inlineQueryResultArticle) {
                        String translatedTitle = bot.i18n()
                                .translate(inlineQueryResultArticle.getTitle(), Locale.ENGLISH);
                        inlineQueryResultArticle.setTitle(translatedTitle);
                        if (inlineQueryResultArticle.getDescription() != null) {
                            inlineQueryResultArticle.setDescription(
                                    bot.i18n().translate(inlineQueryResultArticle.getDescription(), Locale.ENGLISH));
                        }
                        return inlineQueryResultArticle;
                    }
                    return article;
                })
                .toList();


        answerInlineQuery.setResults(translatedArticles);
        try {
            bot.bot().execute(answerInlineQuery);
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
                    .map(h -> senderHolderBotProcessor.getBot(null).i18n().translate(h, locale))
                    .toList();
            return super.build();
        }
    }


}