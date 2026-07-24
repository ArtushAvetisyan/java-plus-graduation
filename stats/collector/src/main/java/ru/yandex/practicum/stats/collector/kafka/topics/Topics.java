package ru.yandex.practicum.stats.collector.kafka.topics;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Topics {

    @Value("${app.kafka.topics.user-actions}")
    private String userActionsTopic;
}