package ru.yandex.practicum.core.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.core.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT MIN(e.createdOn) FROM Event e WHERE e.id IN :eventIds")
    Optional<LocalDateTime> findEarliestCreatedOnByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    List<Event> findAllByInitiatorIdIn(List<Long> userIds);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Boolean existsByCategoryId(Long categoryId);
}