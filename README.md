# ExploreWithMe — Event Discovery & Recommendation Platform

**ExploreWithMe** is a microservice-based platform for discovering events, organizing participation, sharing experiences, and receiving personalized event recommendations.

The platform combines a core event-management domain with an event-driven statistics and recommendation subsystem built on **Apache Kafka** and **gRPC**.

---

## 📋 Table of Contents

- [Platform Overview](#-platform-overview)
- [System Architecture](#-system-architecture)
- [Microservices](#-microservices)
- [Inter-Service Communication](#-inter-service-communication)
- [Statistics & Event-Driven Architecture](#-statistics--event-driven-architecture)
- [Kafka Topics](#-kafka-topics)
- [Processing Configuration](#-processing-configuration)
- [External API](#-external-api)

---

## 📖 Platform Overview

**ExploreWithMe** provides a complete lifecycle for discovering and participating in events:

1. **Event Discovery**: Users can browse events, categories, and event compilations.
2. **Event Participation**: Users can submit participation requests and manage their event registrations.
3. **Social Interaction**: Users can rate and like events, allowing the platform to collect valuable behavioral data.
4. **Recommendations**: User activity is continuously collected and analyzed to calculate event similarity and generate personalized recommendations.

The system is organized into three major areas:

- **`infra`** — infrastructure and service discovery.
- **`core`** — core business logic and event management.
- **`stats`** — statistics collection, event processing, and recommendation services.

---

## 🏗 System Architecture

The platform follows a **microservice architecture** with centralized configuration, service discovery, declarative inter-service communication, and an event-driven statistics pipeline.

### Infrastructure Services (`infra`)

| Service | Technology | Purpose |
| :--- | :--- | :--- |
| **`discovery-server`** | Spring Cloud Eureka | Service registration and discovery. All microservices register with Eureka and use it to locate each other dynamically. |
| **`config-server`** | Spring Cloud Config | Centralized configuration management. All services retrieve their configuration from this server. |
| **`gateway-server`** | Spring Cloud Gateway | Single entry point for external API requests. Runs on port `8080` and routes requests to the appropriate microservices. |

Configuration files for the **`config-server`** are located at:

```text
infra/config-server/src/main/resources/config
```

### Core Business Services and Libraries (`core`)

| Service / Module | Description | Main Responsibilities |
| :--- | :--- | :--- |
| **`interaction-api`** | Shared contract library | Contains DTOs, clients, and common contracts used for inter-service communication. |
| **`event-service`** | Event management service | Manages events, categories, and event compilations (`event`, `category`, `compilation`). |
| **`request-service`** | Participation request service | Manages user requests to participate in events. |
| **`user-service`** | User management service | Handles user administration and user-related operations. |
| **`rating-service`** | Rating and likes service | Provides the event rating and like functionality. |

### Statistics and Recommendation Services (`stats`)

| Service / Module | Description | Main Responsibilities |
| :--- | :--- | :--- |
| **`collector`** | User activity collector | Receives user activity events over gRPC and publishes them to Kafka. |
| **`aggregator`** | Event similarity processor | Processes the Kafka event stream and incrementally calculates event similarity coefficients. |
| **`analyzer`** | Recommendation service | Stores user activity history and similarity data and exposes a gRPC API for recommendations. |
| **`serialization`** | Shared serialization module | Contains Avro and Protocol Buffers schemas used by the statistics pipeline. |
| **`stats-client`** | gRPC client library | Provides gRPC clients for communication between `core` services and the `collector` and `analyzer`. |

---

## 🔗 Inter-Service Communication

Communication between business microservices is implemented declaratively using **Spring Cloud OpenFeign** and **Eureka Service Discovery**.

The shared contracts and clients are maintained in the **`interaction-api`** module, allowing services to communicate through well-defined interfaces.

### Main Service Dependencies

| Service | Communicates With | Purpose |
| :--- | :--- | :--- |
| **`event-service`** | `request-service` | Event participation and request management. |
| **`event-service`** | `rating-service` | Event rating and like operations. |
| **`event-service`** | `user-service` | User-related event operations. |
| **`rating-service`** | `request-service` | Validation of participation requests. |
| **`rating-service`** | `event-service` | Access to event information. |
| **`rating-service`** | `user-service` | User-related validation and operations. |
| **`request-service`** | `event-service` | Event validation and participation management. |
| **`request-service`** | `user-service` | User validation and request ownership. |

This approach keeps service boundaries explicit while avoiding hard-coded service locations through Eureka-based discovery.

---

## 📊 Statistics & Event-Driven Architecture

The statistics and recommendation subsystem is built around an **Event-Driven Architecture** using **Apache Kafka** and **gRPC**.

The processing pipeline can be summarized as:

```text
Core Services
     │
     │ UserActionProto
     ▼
  collector
     │
     │ UserActionAvro
     ▼
 Apache Kafka
     │
     ▼
 aggregator
     │
     │ EventSimilarityAvro
     ▼
 Apache Kafka
     │
     ▼
  analyzer
     │
     ├── PostgreSQL
     │
     └── gRPC Recommendations API
```

### Service Responsibilities

#### `collector`

Receives user activity events as `UserActionProto` messages over **gRPC** and publishes them to Kafka after converting them to the `UserActionAvro` Avro format.

Supported user actions include:

- `VIEW` — a user viewed an event.
- `REGISTER` — a user registered for an event.
- `LIKE` — a user liked an event.

#### `aggregator`

Consumes user activity events from Kafka and incrementally calculates **cosine similarity** between events.

The resulting similarity data is published back to Kafka as `EventSimilarityAvro` events.

#### `analyzer`

Consumes statistics and similarity data from Kafka, persists the information in the database, and exposes a gRPC API for retrieving recommendations and similar events.

The recommendation API is provided through:

```text
RecommendationsController
```

---

## 📨 Kafka Topics

The statistics pipeline uses the following Kafka topics:

| Topic | Message Format | Purpose |
| :--- | :--- | :--- |
| **`stats.user-actions.v1`** | `UserActionAvro` | Transports user activity events such as `VIEW`, `REGISTER`, and `LIKE`. |
| **`stats.events-similarity.v1`** | `EventSimilarityAvro` | Transports calculated similarity scores between pairs of events (`eventA`, `eventB`, `score`). |

### Event Flow

```text
User Actions
     │
     ▼
stats.user-actions.v1
     │
     ▼
  aggregator
     │
     ▼
stats.events-similarity.v1
     │
     ▼
  analyzer
```

This decouples event collection, similarity calculation, and recommendation delivery, allowing each stage to scale independently.

---

## ⚙️ Processing Configuration

Kafka consumers are configured by default with:

- **3 partitions**
- **3 consumer threads**

This configuration enables parallel message processing while providing resilience and better throughput under load.

The number of partitions and consumer threads can be adjusted depending on the expected workload and deployment environment.

---

## 🌐 External API

All external client requests are routed through the **API Gateway** running on port `8080`.

The external API contracts are defined by the following OpenAPI specifications:

- **Main Service API:**  
  [ewm-main-service-spec.json](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json)

- **Statistics Service API:**  
  [ewm-stats-service-spec.json](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json)

The gateway provides a single entry point for clients while hiding the internal microservice topology and service locations.
