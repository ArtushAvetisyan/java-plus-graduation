package ru.yandex.practicum.core.event.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.interaction.dto.event.EventSearchFilterAdmin;
import ru.yandex.practicum.core.interaction.dto.event.EventSearchFilterPublic;
import ru.yandex.practicum.core.interaction.dto.event.EventState;

import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> byAdminFilter(EventSearchFilterAdmin filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.users() != null && !filter.users().isEmpty()) {
                predicates.add(root.get("initiatorId").in(filter.users()));
            }
            if (filter.states() != null && !filter.states().isEmpty()) {
                predicates.add(root.get("eventState").in(filter.states()));
            }
            if (filter.categories() != null && !filter.categories().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.categories()));
            }
            if (filter.rangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), filter.rangeStart()));
            }
            if (filter.rangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), filter.rangeEnd()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> byPublicFilter(EventSearchFilterPublic filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("eventState"), EventState.PUBLISHED));

            if (filter.text() != null && !filter.text().isBlank()) {
                String pattern = "%" + filter.text().toLowerCase() + "%";
                Predicate annotationLike = cb.like(cb.lower(root.get("annotation")), pattern);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(annotationLike, descriptionLike));
            }

            if (filter.categories() != null && !filter.categories().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.categories()));
            }

            if (filter.paid() != null) {
                predicates.add(cb.equal(root.get("paid"), filter.paid()));
            }

            if (filter.rangeStart() == null && filter.rangeEnd() == null) {
                predicates.add(cb.greaterThan(root.get("eventDate"), java.time.LocalDateTime.now()));
            } else {
                if (filter.rangeStart() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), filter.rangeStart()));
                }
                if (filter.rangeEnd() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), filter.rangeEnd()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}