package dev.zagirnur.petbot.sdk.provider;

import lombok.Value;

@Value(staticConstructor = "of")
public class StringUpdateData implements UpdateData {
    private final String data;
}
