package ru.yandex.practicum.stats.deserialization;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserializer extends GeneralAvroDeserializer<UserActionAvro> {
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}