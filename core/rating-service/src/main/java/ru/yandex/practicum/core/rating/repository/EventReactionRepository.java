package ru.yandex.practicum.core.rating.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.rating.model.EventReaction;

import java.util.List;
import java.util.Optional;

public interface EventReactionRepository extends JpaRepository<EventReaction, Long> {

    @Query("SELECT er.reactorId " +
            "FROM EventReaction er " +
            "WHERE er.eventId IN :eventIds " +
            "AND er.reactionType = :reactionType")
    List<Long> findReactorIdsByEventIdAndReactionType(@Param("eventIds") List<Long> eventIds,
                                                      @Param("reactionType") ReactionType reactionType,
                                                      Pageable pageable);

    @Query("SELECT er.eventId FROM EventReaction er WHERE er.reactorId = :userId AND er.reactionType = 'LIKE'")
    List<Long> findFavoriteEventIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT er.eventId " +
            "FROM EventReaction er " +
            "GROUP BY er.eventId " +
            "ORDER BY SUM(CASE WHEN er.reactionType = 'LIKE' THEN 1 " +
            "WHEN er.reactionType = 'DISLIKE' THEN -1 ELSE 0 END) ASC")
    List<Long> findTopEventIdsAsc(Pageable pageable);

    @Query("SELECT er.eventId " +
            "FROM EventReaction er " +
            "GROUP BY er.eventId " +
            "ORDER BY SUM(CASE WHEN er.reactionType = 'LIKE' THEN 1 " +
            "WHEN er.reactionType = 'DISLIKE' THEN -1 ELSE 0 end) DESC")
    List<Long> findTopEventIdsDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN er.reactionType = 'LIKE' THEN 1L ELSE -1L END), 0L) " +
            "FROM EventReaction er " +
            "WHERE er.eventId = :eventId")
    Long getRatingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT er FROM EventReaction er WHERE er.eventId IN :eventIds")
    List<EventReaction> getRatingsByEventIds(@Param("eventIds") List<Long> eventIds);

    Optional<EventReaction> findByReactorIdAndEventId(Long userId, Long eventId);
}