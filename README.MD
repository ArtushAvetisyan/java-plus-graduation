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

---

## 📖 О платформе

Платформа **ExploreWithMe** предоставляет полный спектр возможностей для поиска событий и обмена впечатлениями:
1. **Основной бизнес-контур**: Создание и модерация мероприятий, управление категориями и тематическими подборками (компиляциями), обработка заявок на участие и система пользовательских рейтингов/лайков.
2. **Аналитика и рекомендации**: Непрерывный асинхронный сбор пользовательских действий (`VIEW`, `REGISTER`, `LIKE`), инкрементальный расчет косинусного сходства мероприятий и выдача умных рекомендаций через gRPC API.

---

## 🏗 Архитектурные решения

Архитектура системы логически разделена на три ключевых слоя (`infra`, `core`, `stats`) с применением Event-Driven Architecture и контрактного взаимодействия:

1. **API Gateway Pattern**: Единый входной шлюз **`gateway-server`** (порт `8080`) на базе **Spring Cloud Gateway** осуществляет прием всех внешних клиентских запросов и их маршрутизацию во внутренние микросервисы.
2. **Service Discovery & Config**: Центр управления конфигурациями **Spring Cloud Config** и реестр сервисов **Spring Cloud Eureka** для динамического обнаружения и настройки элементов системы.
3. **Contract Sharing & Schemas (`interaction-api` & `serialization`)**: Вынесение общих DTO, Feign-клиентов и контрактов в библиотеку `interaction-api`, а Avro и Proto схем — в модуль `serialization`.
4. **Event-Driven Recommendation Engine**: Потоковая передача действий пользователей через **gRPC** и **Apache Kafka** с сериализацией в **Apache Avro** для расчета рекомендаций и сходства событий в реальном времени.

---

## 🧩 Состав микросервисов

### 1. Инфраструктурные сервисы (`infra`)
| Сервис / Модуль | Технологии | Назначение и функционал |
| :--- | :--- | :--- |
| **`gateway-server`** | Spring Cloud Gateway | Единый API-шлюз системы (порт `8080`). Принимает все внешние запросы и маршрутизирует их в сервисы. |
| **`discovery-server`** | Spring Cloud Eureka | Сервис регистрации и обнаружения для динамического поиска микросервисов. |
| **`config-server`** | Spring Cloud Config | Централизованный сервер внешних настроек (`infra/config-server/src/main/resources/config`). |

### 2. Основные бизнес-сервисы и библиотеки (`core`)
| Сервис / Модуль | Описание | Основной функционал |
| :--- | :--- | :--- |
| **`interaction-api`** | Общая библиотека | Содержит DTO, Feign-клиенты и общие контракты межсервисного взаимодействия. |
| **`event-service`** | Сервис мероприятий | Управление событиями (`event`), категориями (`category`) и подборками (`compilation`). |
| **`request-service`** | Сервис заявок | Обработка и управление заявками пользователей на участие в мероприятиях. |
| **`user-service`** | Сервис пользователей | Администрирование пользователей и управление профилями. |
| **`rating-service`** | Сервис рейтингов | Система оценок, лайков и формирование рейтинга мероприятий. |

### 3. Сервисы статистики и рекомендаций (`stats`)
| Сервис / Модуль | Технологии | Назначение и функционал |
| :--- | :--- | :--- |
| **`collector`** | Spring Boot, gRPC, Kafka Producer, Avro | Прием событий о действиях пользователей по gRPC (`UserActionProto`) и их публикация в Kafka. |
| **`aggregator`** | Spring Boot, Kafka Consumer/Producer, Avro | Чтение действий из Kafka, инкрементальный расчет косинусного сходства мероприятий. |
| **`analyzer`** | Spring Boot, Kafka Consumer, gRPC, DB | Хранение истории действий и сходств, предоставление gRPC API (`RecommendationsController`). |
| **`serialization`** | Avro, Protobuf | Общий модуль с Avro и Proto схемами данных. |
| **`stats-client`** | Java, gRPC Client | gRPC-клиент для отправки событий и запроса рекомендаций из сервисов модуля `core`. |

---

## 🛠 Технологический стек

- **Язык программирования**: Java 21
- **Основной фреймворк**: Spring Boot 3.x, Spring Framework
- **Микросервисная инфраструктура (Spring Cloud)**:
  - **Spring Cloud Gateway** — шлюз системы на порту `8080`.
  - **Spring Cloud Config** — централизованная конфигурация.
  - **Spring Cloud Netflix Eureka** — обнаружение и регистрация сервисов.
  - **Spring Cloud OpenFeign** — декларативные REST-клиенты для межсервисных вызовов.
- **Транспорт и брокеры данных**:
  - **gRPC / Protocol Buffers** — высокоскоростной бинарный транспорт для передачи действий пользователей и получения рекомендаций.
  - **Apache Kafka** — асинхронная шина событий.
  - **Apache Avro** — сериализация данных с поддержкой схем.
- **База данных**: PostgreSQL / JPA.
- **Спецификации API**: OpenAPI 3.0 / JSON Spec.

---

## 💡 Основной бизнес-контур (Core)

### Ключевые сущности:
- **`event`** — мероприятия с привязкой к локациям, датам, модерации и лимитам участников.
- **`category`** — категории событий.
- **`compilation`** — тематические подборки мероприятий.
- **`request`** — заявка пользователя на участие в событии.
- **`rating`** — пользовательские оценки и лайки.

### Межсервисное взаимодействие (Internal API):
Внутренние вызовы между микросервисами модуля `core` выполняются через **OpenFeign** с использованием поиска сервисов в **Eureka**:
* **`event-service`** ↔ `request-service`, `rating-service`, `user-service`
* **`rating-service`** ↔ `request-service`, `event-service`, `user-service`
* **`request-service`** ↔ `event-service`, `user-service`

---

## ⚡ Контур статистики и рекомендаций (Stats)

Сбор действий пользователей и формирование рекомендаций построены на событийно-ориентированной архитектуре с использованием **Apache Kafka** и **gRPC**:

1. **Сбор событий (`collector`)**: Сервисы `core` через `stats-client` отправляют gRPC-сообщения `UserActionProto` в `collector`, который трансформирует их в Avro (`UserActionAvro`) и публикует в Kafka.
2. **Агрегация (`aggregator`)**: Вычитывает поток действий из Kafka и инкрементально рассчитывает косинусное сходство пар мероприятий (`EventSimilarityAvro`), публикуя результаты в Kafka.
3. **Анализ и рекомендация (`analyzer`)**: Сохраняет поступающие коэффициенты и действия в БД и предоставляет gRPC API (`RecommendationsController`) для получения персональных рекомендаций и похожих событий.

### Конфигурация Kafka Consumers:
* По умолчанию Kafka-консюмеры настроены на работу в **3 партициях** и **3 потоках**, что обеспечивает параллельность и отказоустойчивость обработки сообщений.

---

## 📊 Топики Kafka и форматы данных

1. **`stats.user-actions.v1`**:
  - **Producer**: `collector` | **Consumer**: `aggregator`
  - **Формат**: Apache Avro (`UserActionAvro`)
  - **Содержание**: Передача действий пользователей (`VIEW`, `REGISTER`, `LIKE`).

2. **`stats.events-similarity.v1`**:
  - **Producer**: `aggregator` | **Consumer**: `analyzer`
  - **Формат**: Apache Avro (`EventSimilarityAvro`)
  - **Содержание**: Коэффициенты сходства пар мероприятий (`eventA`, `eventB`, `score`).

---

## 🌐 Внешний API

Все внешние запросы клиентов принимаются через API-шлюз **`gateway-server`** на порту **`8080`**.

* **Спецификация внешнего API (Основной сервис)**: [ewm-main-service-spec.json](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json)
* **Спецификация внешнего API (Сервис статистики)**: [ewm-stats-service-spec.json](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json)