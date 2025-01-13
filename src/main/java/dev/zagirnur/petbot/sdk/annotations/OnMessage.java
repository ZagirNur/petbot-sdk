package dev.zagirnur.petbot.sdk.annotations;

import javax.annotation.RegEx;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface OnMessage {
    /**
     * Если задано, то метод будет срабатывать, 
     * когда сообщение содержит данный command (например, "/start").
     */
    String command() default "";

    /**
     * Если задано, метод будет срабатывать, когда сообщение начинается с prefix.
     */
    String prefix() default "";

    /**
     * Дополнительно можно задать состояние пользователя,
     * в котором данный метод будет срабатывать.
     */
    String state() default "";

    //language=RegExp
    String regexp() default "";
}