package dev.zagirnur.petbot.sdk.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface OnCallback {
    /**
     * Префикс колбэка (CallbackData), который метод будет обрабатывать.
     */
    String prefix();
}