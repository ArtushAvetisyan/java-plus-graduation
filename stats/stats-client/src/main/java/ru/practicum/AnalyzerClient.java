package ru.practicum;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.dashboard.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = analyzerStub.getRecommendationsForUser(request);
            return asStream(iterator);

        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе рекомендаций. ID пользователя - {}", userId, exception);
            return Stream.empty();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка при запросе рекомендаций. ID пользователя - {}", userId, exception);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = analyzerStub.getSimilarEvents(request);
            return asStream(iterator);

        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе похожих событий. ID пользователя - {}, ID события - {}",
                    userId, eventId, exception);
            return Stream.empty();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка при запросе похожих событий. ID пользователя - {}, ID события - {}",
                    userId, eventId, exception);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            Iterator<RecommendedEventProto> iterator = analyzerStub.getInteractionsCount(request);
            return asStream(iterator);

        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе количества взаимодействий для событий. ID события - {}", eventIds, exception);
            return Stream.empty();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка при запросе количества взаимодействий. ID события - {}", eventIds, exception);
            return Stream.empty();
        }
    }

    private <T> Stream<T> asStream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}