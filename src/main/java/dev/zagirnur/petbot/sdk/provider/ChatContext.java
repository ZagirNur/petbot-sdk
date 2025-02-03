package dev.zagirnur.petbot.sdk.provider;

public interface ChatContext {
    String getState();
    void setState(String state);
    void cleanState();
    Long getMessageIdByTag(String tag);
    void tagMessageId(String tag, Long messageId);
    void deleteTag(String tag);
}