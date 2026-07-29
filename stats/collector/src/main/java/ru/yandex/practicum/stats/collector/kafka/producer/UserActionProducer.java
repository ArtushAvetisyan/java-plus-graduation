package ru.yandex.practicum.stats.collector.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.collector.kafka.topics.Topics;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionProducer {
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;
    private final Topics topics;

    public void send(UserActionAvro userActionAvro) {
        Long key = userActionAvro.getEventId();
        String topic = topics.getUserActionsTopic();
        log.debug("Отправка события в Kafka топик - {}. ID пользователя - {}, ID события - {}, тип действия - {}",
                topics.getUserActionsTopic(), userActionAvro.getUserId(), userActionAvro.getEventId(), userActionAvro.getActionType());

        kafkaTemplate.send(topic, key, userActionAvro).whenComplete((
                result, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке события в Kafka! Топик - {}. Ошибка: {}", topic, exception.getMessage());
            } else {
                log.debug("Действие успешно отправлено в топик - {}", topic);
            }
        });
    }
}