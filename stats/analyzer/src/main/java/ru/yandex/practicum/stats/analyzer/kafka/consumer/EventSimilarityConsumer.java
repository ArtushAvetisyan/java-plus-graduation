package ru.yandex.practicum.stats.analyzer.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.analyzer.service.similarity.EventSimilarityService;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventSimilarityConsumer {
    private final EventSimilarityService eventSimilarityService;

    @KafkaListener(
            topics = "${app.kafka.topics.events-similarity}",
            groupId = "${app.kafka.group-ids.events-similarity}",
            concurrency = "${app.kafka.concurrency:3}")
    public void listen(EventSimilarityAvro eventSimilarity) {
        log.info("Получено сообщение о сходстве мероприятий (analyzer): Событие A - {}, событие В - {}",
                eventSimilarity.getEventA(), eventSimilarity.getEventB());
        eventSimilarityService.processSimilarity(eventSimilarity);
    }
}