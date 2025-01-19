package dev.zagirnur.petbot.sdk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

@Value(staticConstructor = "of")
public class StringUpdateData implements UpdateData{
    private final String data;
}
