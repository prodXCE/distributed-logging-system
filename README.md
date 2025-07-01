---

# Distributed Logging System with Spring Boot, Kafka & ELK

I built this project to implement a complete, production-grade distributed logging pipeline. The primary goal was to create a system capable of ingesting logs from multiple microservices and making them centrally available for search and analysis. The architecture is designed for resilience and scalability.

---

## 🏗️ Architecture

The system follows a decoupled, asynchronous data flow to ensure that log-producing applications are not impacted by the performance or availability of the logging backend.

```
+-----------------+      +------------------------+      +-----------------+      +-----------------+      +---------------------+      +----------------+
|                 |      |                        |      |                 |      |                 |      |                     |      |                |
|  Applications   |----->| Spring Boot Log        |----->|  Apache Kafka   |----->|    Logstash     |----->|    Elasticsearch    |----->|     Kibana     |
| (Log Sources)   |      |   Aggregator           |      | (Message Queue) |      | (ETL Processor) |      | (Storage & Index)   |      | (UI/Explorer)  |
|                 |      |                        |      |                 |      |                 |      |                     |      |                |
+-----------------+      +------------------------+      +-----------------+      +-----------------+      +---------------------+      +----------------+
```

---

## 🧩 Component Roles

* **Spring Boot Log Aggregator**:
  A custom Java application that serves as the primary ingestion point. It exposes an HTTP endpoint, performs initial log structuring, and forwards the data to Kafka.

* **Apache Kafka**:
  A distributed, fault-tolerant message broker. Used to buffer incoming logs, decoupling ingestion from processing and preventing data loss.

* **Logstash**:
  The data processing pipeline. It consumes logs from Kafka, performs complex parsing and enrichment using Grok and other filters, and forwards structured data to Elasticsearch.

* **Elasticsearch**:
  A distributed search and analytics engine used for storing and indexing structured log data for fast retrieval.

* **Kibana**:
  A web interface for querying, visualizing, and analyzing logs stored in Elasticsearch.

* **Docker & Docker Compose**:
  The entire backend infrastructure is containerized and managed via Docker Compose for simplified setup and environment consistency.

---

## ⚙️ Tech Stack

* **Backend**: Java 24, Spring Boot 3
* **Messaging**: Apache Kafka, Zookeeper
* **Data Processing**: Logstash
* **Storage & Search**: Elasticsearch
* **UI**: Kibana
* **Build Tool**: Apache Maven
* **Containerization**: Docker & Docker Compose

---

## 🚀 System Setup and Execution

### 🔧 Prerequisites

Make sure the following tools are installed:

* Java 24 or newer
* Apache Maven
* Docker
* Docker Compose

---

### 1. Launch Backend Infrastructure

Run the backend services using Docker Compose:

```bash
docker-compose up -d
```

This command starts all required containers in **detached mode**.

---

### 2. Run the Log Aggregator Application

With the backend running:

1. Open the project in your IDE (e.g., IntelliJ IDEA).
2. Run the `LogAggregatorApplication.java` main class.
3. The app will start on port `8080`.

Your system is now fully operational.

---

## 📤 Sending Logs to the System

Logs are ingested via HTTP POST to:

```
http://localhost:8080/api/v1/logs/{log-source-name}
```

> Replace `{log-source-name}` with your application's name.

---

### 🔍 Example Log Submissions

#### ✅ Standard Application Log (Tests Grok filter)

```bash
curl -X POST -H "Content-Type: text/plain" \
--data "WARN [http-nio-8080-exec-7] com.example.payment.StripeGateway - Payment gateway timeout for transactionId=ch_3PqF2j2eZvKYlo2C1g3711fF." \
http://localhost:8080/api/v1/logs/payment-service
```

---

#### 🔑 Key-Value Log (Tests KV filter)

```bash
curl -X POST -H "Content-Type: text/plain" \
--data "level=INFO service=auth-service operation=userLogin transactionId=b4a5e8f1-a2d3-4c5e-9f0a-1b2c3d4e5f6a userId=alex.davis@example.com message=\"User login successful\"" \
http://localhost:8080/api/v1/logs/auth-service
```

---

#### 🧵 Multi-line Stack Trace

> Uses `$'...'` to handle newlines.

```bash
curl -X POST -H "Content-Type: text/plain" \
--data $'ERROR [db-pool-t1] com.example.inventory.StockRepository - Failed to update stock for productId=PROD-55432\njava.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'55432\' for key \'PRIMARY\'\n\tat com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:123)\n\tat com.zaxxer.hikari.pool.ProxyStatement.execute(ProxyStatement.java:94)\n\t... 14 more' \
http://localhost:8080/api/v1/logs/inventory-service
```

---

## 📊 Accessing Logs via Kibana

1. **Navigate to Kibana**
   Visit: [http://localhost:5601](http://localhost:5601)

2. **Create a Data View**

   * Go to **Stack Management > Data Views**
   * Click **Create data view**
   * Set the index pattern to: `logs-*`
   * Select `@timestamp` as the primary time field
   * Save the view

3. **Explore Data**

   * Go to the **Discover** tab
   * Use the time filter (top-right) to select the log time range
   * Use queries like:

     ```
     level : ERROR
     ```

   to filter logs by level.

---

