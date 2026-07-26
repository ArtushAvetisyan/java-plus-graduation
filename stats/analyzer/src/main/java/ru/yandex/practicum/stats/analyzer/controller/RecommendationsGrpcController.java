package ru.yandex.practicum.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.dashboard.*;
import ru.yandex.practicum.stats.analyzer.service.recommendation.RecommendationService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsGrpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> response) {
        log.info("Получен gRPC запрос рекомендаций. ID пользователя - {}", request.getUserId());

        try {
            recommendationService.getRecommendationsForUser(
                            request.getUserId(), request.getMaxResults())
                    .forEach(response::onNext);
            response.onCompleted();

        } catch (Exception exception) {
            log.error("Ошибка при получении рекомендаций: {}", exception.getMessage());
            response.onError(Status.INTERNAL
                    .withDescription("Ошибка при получении рекомендаций")
                    .withCause(exception)
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> response) {
        log.info("Получен gRPC запрос похожих событий. ID пользователя - {}, ID события - {}",
                request.getUserId(), request.getEventId());

        try {
            recommendationService.getSimilarEvents(
                            request.getEventId(), request.getUserId(), request.getMaxResults())
                    .forEach(response::onNext);
            response.onCompleted();

        } catch (Exception exception) {
            log.error("Ошибка при получении похожих событий: {}", exception.getMessage());
            response.onError(Status.INTERNAL
                    .withDescription("Ошибка при получении похожих событий")
                    .withCause(exception)
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> response) {
        log.info("Получен gRPC запрос взаимодействий. ID событий - {}", request.getEventIdList());

        try {
            recommendationService.getInteractionsCount(request.getEventIdList())
                    .forEach(response::onNext);
            response.onCompleted();

        } catch (Exception exception) {
            log.error("Ошибка при получении суммы взаимодействий: {}", exception.getMessage());
            response.onError(Status.INTERNAL
                    .withDescription("Ошибка при получении суммы взаимодействий")
                    .withCause(exception)
                    .asRuntimeException());
        }
    }
}