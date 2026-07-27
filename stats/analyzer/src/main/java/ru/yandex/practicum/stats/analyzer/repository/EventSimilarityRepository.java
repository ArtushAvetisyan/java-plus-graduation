package ru.yandex.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("SELECT es FROM EventSimilarity es WHERE es.eventA = :eventId OR es.eventB = :eventId")
    List<EventSimilarity> findAllByEventId(@Param("eventId") long eventId);

    @Query("SELECT es " +
            "FROM EventSimilarity es " +
            "WHERE es.eventA IN :eventIds OR es.eventB IN :eventIds")
    List<EventSimilarity> findAllByEventIds(Collection<Long> eventIds);

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);
}