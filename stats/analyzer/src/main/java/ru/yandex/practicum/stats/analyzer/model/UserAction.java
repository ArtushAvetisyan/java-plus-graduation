package ru.yandex.practicum.stats.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_actions",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_event", columnNames = {"userId", "eventId"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "eventId", nullable = false)
    private Long eventId;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}