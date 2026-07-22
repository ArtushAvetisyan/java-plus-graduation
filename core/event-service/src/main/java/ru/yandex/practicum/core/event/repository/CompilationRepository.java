package ru.yandex.practicum.core.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.core.event.model.Compilation;

import java.util.Collection;
import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Compilation c " +
            "WHERE c.title = :title " +
            "AND (SELECT COUNT(e) FROM c.events e WHERE e.id IN :eventIds) = :size " +
            "AND (SELECT COUNT(e) FROM c.events e) = :size")
    boolean existsByTitleAndSameEvents(@Param("title") String title,
                                       @Param("eventIds") Collection<Long> eventIds,
                                       @Param("size") long size);


    @Query("SELECT c FROM Compilation c " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned) " +
            "ORDER BY c.id ASC")
    List<Compilation> findNeededCompilations(@Param("pinned") Boolean pinned, Pageable pageable);
}