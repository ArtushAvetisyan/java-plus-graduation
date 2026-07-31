package ru.yandex.practicum.core.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.CollectorClient;
import ru.yandex.practicum.core.interaction.config.DateTimeConfig;
import ru.yandex.practicum.core.interaction.handler.ErrorHandler;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.client")
@Import({DateTimeConfig.class, ErrorHandler.class, CollectorClient.class})
@EnableDiscoveryClient
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}