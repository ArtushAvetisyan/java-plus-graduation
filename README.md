# ExploreWithMe

This project is a system for searching for events and sharing impressions.

---

## 1. System Architecture

The project is divided into grouping modules `infra` (infrastructure), `core` (main business logic), and `stats` (statistics and recommendation services).

### Infrastructure Services (`infra`)

* **`discovery-server` (Spring Cloud Eureka):** Registration and discovery service. All microservices register here
  and find each other dynamically.
* **`config-server` (Spring Cloud Config):** Centralized configuration server. All services receive their settings
  from this server.
    * *Location of configuration files:* `infra/config-server/src/main/resources/config`.
* **`gateway-server` (Spring Cloud Gateway):** The system's single API gateway, running on port `8080`. It accepts all
  external client requests and routes them to the corresponding microservices.

### Business Services and Libraries (`core`)

* **`interaction-api`:** Shared library containing DTOs, clients, and common interservice interaction contracts.
* **`event-service`:** Management of events, categories, and event compilations (entities `event`, `category`,
  `compilation`).
* **`request-service`:** Management of participation requests for events.
* **`user-service`:** User administration.
* **`rating-service`:** Rating/like system.

### Statistics and Recommendation Services (`stats`)

* **`collector`:** Service for receiving user action events via gRPC and publishing them to Kafka.
* **`aggregator`:** Service for calculating event similarity coefficients based on the event stream from Kafka.
* **`analyzer`:** Service for storing the history of user actions and similarity coefficients, providing a gRPC API
  for recommendations.
* **`serialization`:** Shared module containing Avro and Proto data schemas.
* **`stats-client`:** gRPC client for interaction between services of the `core` module and the collector and analyzer.

---

## 2. Internal API (Interservice Interaction)

Interaction between microservices is carried out declaratively through **OpenFeign** using service discovery in
**Eureka**.

### Main internal contracts:

1. **`event-service` ↔ `request-service`**, **`event-service` ↔ `rating-service`**, **`event-service` ↔ `user-service`**

---

2. **`rating-service` ↔ `request-service`**, **`rating-service` ↔ `event-service`**, **`rating-service` ↔
   `user-service`**

---

3. **`request-service` ↔ `event-service`**, **`request-service` ↔ `user-service`**

---

## 3. Stats Services and Event-Driven Architecture (Apache Kafka & gRPC)

User action collection and recommendation generation are built on an event-driven architecture using
**Apache Kafka** and **gRPC**.

### Service Purposes:

* **`collector`:** Receives messages about user actions (`UserActionProto`) via gRPC and publishes them to Kafka in
  Avro format (`UserActionAvro`).
* **`aggregator`:** Reads actions from Kafka, incrementally calculates the cosine similarity of events, and sends
  the results (`EventSimilarityAvro`) to Kafka.
* **`analyzer`:** Stores incoming data from Kafka in the database and provides a gRPC API (`RecommendationsController`) for
  obtaining recommendations and similar events.

### Kafka Topics:

* **`stats.user-actions.v1`:** Topic for transmitting user actions (`VIEW`, `REGISTER`, `LIKE`) in Avro format.
* **`stats.events-similarity.v1`:** Topic for transmitting calculated similarity coefficients of event pairs (`eventA`,
  `eventB`, `score`).

### Processing Configuration:

* By default, Kafka consumers are configured to work with **3 partitions** and **3 threads**, which provides parallelism and
  fault tolerance in message processing.

---

## 4. External API

All external requests are accepted through the API gateway on port `8080`.

* **External API specification (Main
  service):** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json
* **External API specification (Statistics
  service):** https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json
