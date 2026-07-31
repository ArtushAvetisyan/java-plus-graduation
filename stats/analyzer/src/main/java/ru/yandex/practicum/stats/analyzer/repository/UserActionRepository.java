package ru.yandex.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.repository.projections.EventWeightSumProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    @Query("SELECT ua.eventId " +
            "FROM UserAction ua " +
            "WHERE ua.userId = :userId")
    Set<Long> findInteractedEventIdsByUserId(@Param("userId") long userId);

    @Query("SELECT ua.eventId AS eventId, SUM(ua.maxWeight) AS totalWeight " +
            "FROM UserAction ua " +
            "WHERE ua.eventId IN :eventIds " +
            "GROUP BY ua.eventId")
    List<EventWeightSumProjection> sumWeightsByEventIds(@Param("eventIds") List<Long> eventIds);

    List<UserAction> findByUserId(Long userId);

    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);
}