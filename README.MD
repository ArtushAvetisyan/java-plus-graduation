# ExploreWithMe

Данный проект представляет собой систему для поиска событий и обмена впечатлениями.

---

## 1. Архитектура системы

Проект разделен на группирующие модули `infra` (инфраструктура), `core` (основная бизнес-логика) и `stats`(сервис статистики).

### Инфраструктурные сервисы (`infra`)
* **`discovery-server` (Spring Cloud Eureka):** Сервис регистрации и обнаружения. Все микросервисы регистрируются здесь и находят друг друга динамически.
* **`config-server` (Spring Cloud Config):** Централизованный сервер конфигурации. Все сервисы получают свои настройки из этого сервера.
    * *Расположение конфигурационных файлов:* `infra/config-server/src/main/resources/config`.
* **`gateway-server` (Spring Cloud Gateway):** Единый API-шлюз системы, работающий на порту `8080`. Принимает все внешние запросы клиентов и маршрутизирует их в соответствующие микросервисы.

### Бизнес-сервисы (`core`)
* **`event-service`:** Управление мероприятиями, категориями и подборками событий (сущности `event`, `category`, `compilation`).
* **`request-service`:** Управление заявками на участие в мероприятиях.
* **`user-service`:** Администрирование пользователей.
* **`rating-service`:** Система рейтингов/лайков.

### Сервис-статистики (`stats`)
* **`stats-server`:** Сервис сбора и предоставления статистики посещений.
---

## 2. Внутренний API (Межсервисное взаимодействие)

Взаимодействие между микросервисами осуществляется декларативно через **OpenFeign** с использованием поиска сервисов в **Eureka**.

### Основные внутренние контракты:

1. **`event-service` ↔ `request-service`**, **`event-service` ↔ `rating-service`**, **`event-service` ↔ `user-service`**
---
2. **`rating-service` ↔ `request-service`**, **`rating-service` ↔ `event-service`**, **`rating-service` ↔ `user-service`**
---
3. **`request-service` ↔ `event-service`**, **`request-service` ↔ `user-service`**

---

## 3. Внешний API

Все внешние запросы принимаются через API-шлюз на порту `8080`.

* **Спецификация внешнего API:** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json
* **Спецификация внешнего API:** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json
