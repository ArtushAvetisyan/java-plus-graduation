package ru.yandex.practicum.stats.analyzer.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.analyzer.service.action.UserActionService;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserActionConsumer {
    private final UserActionService userActionService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-actions}",
            groupId = "${app.kafka.group-ids.user-actions}",
            containerFactory = "userActionKafkaListenerContainerFactory")
    public void listen(UserActionAvro action) {
        log.debug("Получено действие пользователя (analyzer): ID пользователя - {}, ID события - {}, тип события - {}",
                action.getUserId(), action.getEventId(), action.getActionType());
        userActionService.processAction(action);
    }
}