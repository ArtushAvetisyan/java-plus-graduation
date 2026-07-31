package ru.yandex.practicum.stats.analyzer.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.repository.projections.SimilarEventProjection;

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

    @Query("""
            SELECT CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END AS eventId, es.score AS score
            FROM EventSimilarity es
            WHERE (es.eventA = :eventId OR es.eventB = :eventId)
            AND CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END NOT IN (
            SELECT ua.eventId
            FROM UserAction ua
            WHERE ua.userId = :userId)
            ORDER BY es.score DESC
            """)
    List<SimilarEventProjection> findSimilarEventsForUser(@Param("eventId") long eventId,
                                                          @Param("userId") long userId,
                                                          Limit limit);

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);
}