package ru.yandex.practicum.stats.aggregator.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.aggregator.kafka.producer.EventSimilarityProducer;
import ru.yandex.practicum.stats.aggregator.service.AggregatorService;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserActionListener {

    private final EventSimilarityProducer similarityProducer;
    private final AggregatorService aggregatorService;

    @KafkaListener(topics = "#{topics.userActionsTopic}")
    public void listen(UserActionAvro action) {
        log.info("Получено действие пользователя: ID пользователя - {}, ID события - {}, тип события - {}",
                action.getUserId(), action.getEventId(), action.getActionType());

        List<EventSimilarityAvro> updatedSimilarities = aggregatorService.processUserAction(action);
        if (!updatedSimilarities.isEmpty()) {
            for (EventSimilarityAvro similarity : updatedSimilarities) {
                similarityProducer.send(similarity);
            }
            log.info("Отправлено {} обновлений сходств для события - {}", updatedSimilarities.size(), action.getEventId());
        }
    }
}