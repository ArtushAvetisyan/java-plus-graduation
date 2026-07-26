package ru.yandex.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;

import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);
}