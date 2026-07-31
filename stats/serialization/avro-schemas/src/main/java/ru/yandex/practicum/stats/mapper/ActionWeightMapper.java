package ru.yandex.practicum.stats.mapper;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public class ActionWeightMapper {

    private static final double UNRECOGNIZED = 0.0;
    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    public static double getWeight(ActionTypeAvro actionType) {
        if (actionType == null) return UNRECOGNIZED;

        return switch (actionType) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }
}