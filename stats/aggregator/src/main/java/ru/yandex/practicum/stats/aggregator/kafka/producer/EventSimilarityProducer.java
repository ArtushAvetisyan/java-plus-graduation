package ru.yandex.practicum.stats.aggregator.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.aggregator.kafka.topics.Topics;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventSimilarityProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    private final Topics topics;

    public void send(EventSimilarityAvro similarityAvro) {
        String key = similarityAvro.getEventA() + ":" + similarityAvro.getEventB();
        String topic = topics.getEventsSimilarityTopic();
        log.info("Отправка коэффициента сходства в Kafka топик - {}. ID события А - {}, ID события B - {}",
                topic, similarityAvro.getEventA(), similarityAvro.getEventB());

        kafkaTemplate.send(topic, key, similarityAvro).whenComplete((
                result, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке коэффициента сходства в Kafka! Топик - {}. Ошибка - {}",
                        topic, exception.getMessage());
            } else {
                log.info("Коэффициент сходства успешно отправлено в топик - {}", topic);
            }
        });
    }
}