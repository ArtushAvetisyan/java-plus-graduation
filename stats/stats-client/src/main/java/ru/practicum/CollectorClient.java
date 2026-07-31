package ru.practicum;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.collector.UserActionProto;

import java.time.Instant;

@Service
@Slf4j
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType, Instant timestamp) {
        try {
            Timestamp protoTimestamp = Timestamp.newBuilder()
                    .setSeconds(timestamp.getEpochSecond())
                    .setNanos(timestamp.getNano())
                    .build();

            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(protoTimestamp)
                    .build();

            collectorStub.collectUserAction(request);

        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при отправке действия пользователя. ID пользователя - {}, ID события - {}",
                    userId, eventId, exception);
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка при отправке действия пользователя. ID пользователя - {}, ID события - {}",
                    userId, eventId, exception);
        }
    }
}