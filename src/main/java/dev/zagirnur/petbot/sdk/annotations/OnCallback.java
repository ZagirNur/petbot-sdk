package dev.zagirnur.petbot.sdk.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Repeatable(ContainerForOnCallback.class)
public @interface OnCallback {
    String prefix() default "";
}