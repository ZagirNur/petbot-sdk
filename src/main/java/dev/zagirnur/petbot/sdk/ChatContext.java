package dev.zagirnur.petbot.sdk;

public interface ChatContext {

    /**
     * Возвращает текущее состояние.
     *
     * @return текущее состояние
     */
    String getState();

    /**
     * Устанавливает новое состояние.
     *
     * @param state новое состояние
     */
    void setState(String state);

    /**
     * Очищает состояние.
     */
    void cleanState();
}