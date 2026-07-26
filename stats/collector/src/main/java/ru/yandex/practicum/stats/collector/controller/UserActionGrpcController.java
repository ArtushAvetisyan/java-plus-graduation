package ru.yandex.practicum.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;
import ru.yandex.practicum.stats.collector.kafka.producer.UserActionProducer;
import ru.yandex.practicum.stats.collector.mapper.UserActionMapper;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionGrpcController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionMapper actionMapper;
    private final UserActionProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> response) {
        log.info("Получен gRPC запрос на действие пользователя: ID пользователя - {}, ID события - {}, тип действия - {}",
                request.getUserId(), request.getEventId(), request.getActionType());

        try {
            UserActionAvro userActionAvro = actionMapper.toActionAvro(request);
            producer.send(userActionAvro);
            response.onNext(Empty.getDefaultInstance());
            response.onCompleted();

        } catch (Exception exception) {
            log.error("Ошибка при обработке пользовательского действия: {}", exception.getMessage());
            response.onError(Status.INTERNAL
                    .withDescription("Не удалось обработать действие")
                    .withCause(exception)
                    .asRuntimeException()
            );
        }
    }
}