# ExploreWithMe — Microservices Event & Recommendation Platform

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-blue?style=flat-square&logo=spring)
![gRPC](https://img.shields.io/badge/gRPC-Framework-blue?style=flat-square&logo=grpc)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Avro-red?style=flat-square&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue?style=flat-square&logo=postgresql)

Платформа **ExploreWithMe** — это микросервисная система для поиска интересных мероприятий, организации совместного досуга, управления заявками на участие, оценки событий и получения персональных рекомендаций на основе потоковой аналитики действий пользователей.

---

## 📋 Оглавление
- [О платформе](#-о-платформе)
- [Архитектурные решения](#-архитектурные-решения)
- [Состав микросервисов](#-состав-микросервисов)
- [Технологический стек](#-технологический-стек)
- [Основной бизнес-контур (Core)](#-основной-бизнес-контур-core)
- [Контур статистики и рекомендаций (Stats)](#-контур-статистики-и-рекомендаций-stats)
- [Топики Kafka и форматы данных](#-топики-kafka-и-форматы-данных)
- [Внешний API](#-внешний-api)

Проект разделен на группирующие модули `infra` (инфраструктура), `core` (основная бизнес-логика) и `stats` (сервисы
статистики и рекомендаций).

### Инфраструктурные сервисы (`infra`)

* **`discovery-server` (Spring Cloud Eureka):** Сервис регистрации и обнаружения. Все микросервисы регистрируются здесь
  и находят друг друга динамически.
* **`config-server` (Spring Cloud Config):** Централизованный сервер конфигурации. Все сервисы получают свои настройки
  из этого сервера.
    * *Расположение конфигурационных файлов:* `infra/config-server/src/main/resources/config`.
* **`gateway-server` (Spring Cloud Gateway):** Единый API-шлюз системы, работающий на порту `8080`. Принимает все
  внешние запросы клиентов и маршрутизирует их в соответствующие микросервисы.

### Бизнес-сервисы и библиотеки (`core`)

* **`interaction-api`:** Общая библиотека, содержащая DTO, клиенты и общие контракты межсервисного взаимодействия.
* **`event-service`:** Управление мероприятиями, категориями и подборками событий (сущности `event`, `category`,
  `compilation`).
* **`request-service`:** Управление заявками на участие в мероприятиях.
* **`user-service`:** Администрирование пользователей.
* **`rating-service`:** Система рейтингов/лайков.

### Сервисы статистики и рекомендаций (`stats`)

* **`collector`:** Сервис приема событий о действиях пользователей по gRPC и их публикации в Kafka.
* **`aggregator`:** Сервис вычисления коэффициентов сходства мероприятий на основе потока событий из Kafka.
* **`analyzer`:** Сервис хранения истории действий пользователей и коэффициентов сходства, предоставляющий gRPC API
  рекомендаций.
* **`serialization`:** Общий модуль, содержащий Avro и Proto схемы данных.
* **`stats-client`:** gRPC-клиент для взаимодействия сервисов модуля `core` с коллектором и анализатором.

---

## 🏗 Архитектурные решения

Взаимодействие между микросервисами осуществляется декларативно через **OpenFeign** с использованием поиска сервисов в
**Eureka**.

1. **API Gateway Pattern**: Единый входной шлюз **`gateway-server`** (порт `8080`) на базе **Spring Cloud Gateway** осуществляет прием всех внешних клиентских запросов и их маршрутизацию во внутренние микросервисы.
2. **Service Discovery & Config**: Центр управления конфигурациями **Spring Cloud Config** и реестр сервисов **Spring Cloud Eureka** для динамического обнаружения и настройки элементов системы.
3. **Contract Sharing & Schemas (`interaction-api` & `serialization`)**: Вынесение общих DTO, Feign-клиентов и контрактов в библиотеку `interaction-api`, а Avro и Proto схем — в модуль `serialization`.
4. **Event-Driven Recommendation Engine**: Потоковая передача действий пользователей через **gRPC** и **Apache Kafka** с сериализацией в **Apache Avro** для расчета рекомендаций и сходства событий в реальном времени.

1. **`event-service` ↔ `request-service`**, **`event-service` ↔ `rating-service`**, **`event-service` ↔ `user-service`**

---

2. **`rating-service` ↔ `request-service`**, **`rating-service` ↔ `event-service`**, **`rating-service` ↔
   `user-service`**

---

3. **`request-service` ↔ `event-service`**, **`request-service` ↔ `user-service`**

---

## 3. Сервисы модуля stats и событийно-ориентированная архитектура (Apache Kafka & gRPC)

Сбор действий пользователей и формирование рекомендаций построены на событийно-ориентированной архитектуре с
использованием **Apache Kafka** и **gRPC**.

### Назначение сервисов:

* **`collector`:** Принимает сообщения о действиях пользователей (`UserActionProto`) по gRPC и публикует их в Kafka в
  формате Avro (`UserActionAvro`).
* **`aggregator`:** Читает действия из Kafka, инкрементально рассчитывает косинусное сходство мероприятий и отправляет
  результаты (`EventSimilarityAvro`) в Kafka.
* **`analyzer`:** Сохраняет поступающие данные из Kafka в БД и предоставляет gRPC API (`RecommendationsController`) для
  получения рекомендаций и похожих событий.

### Kafka Topics (Топики):

* **`stats.user-actions.v1`:** Топик для передачи пользовательских действий (`VIEW`, `REGISTER`, `LIKE`) в формате Avro.
* **`stats.events-similarity.v1`:** Топик для передачи рассчитанных коэффициентов сходства пар мероприятий (`eventA`,
  `eventB`, `score`).

### Конфигурация обработки:

* По умолчанию Kafka-консюмеры настроены на работу в **3 партициях** и **3 потоках**, что обеспечивает параллельность и
  отказоустойчивость обработки сообщений.

---

## 4. Внешний API

Все внешние запросы клиентов принимаются через API-шлюз **`gateway-server`** на порту **`8080`**.

* **Спецификация внешнего API (Основной
  сервис):** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json
* **Спецификация внешнего API (Сервис
  статистики):** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json
