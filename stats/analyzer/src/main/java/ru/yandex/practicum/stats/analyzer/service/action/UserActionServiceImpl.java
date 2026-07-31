package ru.yandex.practicum.stats.analyzer.service.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.repository.UserActionRepository;
import ru.yandex.practicum.stats.mapper.ActionWeightMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionServiceImpl implements UserActionService {
    private final UserActionRepository repository;

    @Override
    @Transactional
    public void processAction(UserActionAvro action) {
        double newWeight = ActionWeightMapper.getWeight(action.getActionType());
        Instant actionTimestamp = action.getTimestamp();

        repository.findByUserIdAndEventId(action.getUserId(), action.getEventId()).ifPresentOrElse(
                existing -> {
                    if (newWeight > existing.getMaxWeight()) {
                        existing.setMaxWeight(newWeight);
                        existing.setUpdatedAt(actionTimestamp);
                        repository.save(existing);

                        log.debug("Обновлен максимальный вес действия. ID пользователя - {}, ID события - {}",
                                action.getUserId(), action.getEventId());
                    }
                },
                () -> {
                    UserAction userAction = UserAction.builder()
                            .userId(action.getUserId())
                            .eventId(action.getEventId())
                            .maxWeight(newWeight)
                            .updatedAt(actionTimestamp)
                            .build();

                    repository.save(userAction);
                    log.debug("Зарегистрировано новое действие. ID пользователя - {}, ID события - {}",
                            action.getUserId(), action.getEventId());
                });
    }
}