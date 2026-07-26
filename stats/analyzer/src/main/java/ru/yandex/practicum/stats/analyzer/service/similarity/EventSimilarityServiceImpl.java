package ru.yandex.practicum.stats.analyzer.service.similarity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.repository.EventSimilarityRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final EventSimilarityRepository repository;

    @Override
    @Transactional
    public void processSimilarity(EventSimilarityAvro eventSimilarity) {
        Instant eventTimestamp = eventSimilarity.getTimestamp();

        repository.findByEventAAndEventB(eventSimilarity.getEventA(), eventSimilarity.getEventB()).ifPresentOrElse(
                existing -> {
                    existing.setScore(eventSimilarity.getScore());
                    existing.setUpdatedAt(eventTimestamp);
                    repository.save(existing);

                    log.info("Обновлен коэффициент сходства для пары. Событие А - {}, событие В - {}",
                            eventSimilarity.getEventA(), eventSimilarity.getEventB());
                },
                () -> {
                    EventSimilarity newSimilarity = EventSimilarity.builder()
                            .eventA(eventSimilarity.getEventA())
                            .eventB(eventSimilarity.getEventB())
                            .score(eventSimilarity.getScore())
                            .updatedAt(eventTimestamp)
                            .build();
                    repository.save(newSimilarity);

                    log.info("Сохранен новый коэффициент сходства для пары. Событие А - {}, событие В - {}",
                            eventSimilarity.getEventA(), eventSimilarity.getEventB());
                }
        );
    }
}