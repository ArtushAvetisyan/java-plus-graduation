package ru.yandex.practicum.core.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.core.interaction.config.DateTimeConfig;
import ru.yandex.practicum.core.interaction.handler.ErrorHandler;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.core.event", "ru.practicum"})
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.client")
@Import({DateTimeConfig.class, ErrorHandler.class})
@EnableDiscoveryClient
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}