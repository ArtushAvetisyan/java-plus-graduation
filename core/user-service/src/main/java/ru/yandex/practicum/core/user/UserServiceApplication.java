package ru.yandex.practicum.core.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.core.interaction.config.DateTimeConfig;
import ru.yandex.practicum.core.interaction.handler.ErrorHandler;

@SpringBootApplication
@EnableDiscoveryClient
@Import({DateTimeConfig.class, ErrorHandler.class})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}