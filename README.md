# RMS to PriceLogix Integration System

## Overview

This system facilitates seamless integration between the **Retail Management Suite (RMS)** and the **PriceLogix Enterprise Optimizer**. It implements a distributed processing architecture to efficiently handle retail price adjustments across multiple store locations while providing actionable insights to optimize pricing decisions. The system focuses on:

- **Distributed Price Adjustment Schedule (PAS) Processing:** Multiple service instances handle dedicated store groups, each running scheduled data retrieval and processing operations.
- **Coordinated Price Adjustment Processing:** Centralized handling of Price Adjustment Directives (PAD) and Price Restoration Actions (PRA) through a message queue architecture.
- **Asynchronous Communication:** RabbitMQ enables reliable data exchange between distributed RMS Integration Service instances and the PriceLogix Feed Service.

## Architecture

The system implements a distributed processing model with three main components:

1. **RMS Integration Service Instances:**
   - Multiple instances operate independently
   - Each instance handles a specific group of store locations
   - Daily scheduled data retrieval and processing
   - Publishes processed data to centralized message queue

2. **Message Queue Layer:**
   - RabbitMQ coordinates communication between services
   - Handles message routing from multiple publishers
   - Ensures orderly processing of messages
   - Implements robust error handling and retry logic

3. **PriceLogix Feed Service:**
   - Single consumer processing messages from all RMS Integration Service instances
   - Implements standardized data transformation
   - Manages output generation and delivery to PriceLogix

## Features

- **Distributed Data Integration:** Multiple service instances process and transform data from assigned store groups into a standardized format for PriceLogix.
- **Load-Balanced Architecture:** Staggered scheduling of service instances prevents system-wide resource contention.
- **Centralized Queue Management:** RabbitMQ coordinates message flow from multiple sources while maintaining processing order.
- **Comprehensive Monitoring:** Integrated Prometheus and Grafana provide visibility into individual instance and system-wide performance.
- **Enhanced Resilience:** Dead Letter Queues (DLQs) and robust error handling ensure reliability across all processing instances.

## Technology Stack

- **Programming Language:** Java 21 with Spring Boot 3
- **Message Queue:** RabbitMQ with TLS encryption
- **Monitoring and Visualization:** Prometheus, RabbitMQ Exporter, Grafana
- **Logging:** Loki for centralized log management
- **Build Tool:** Gradle
- **Containerization:** Docker Compose
- **CI/CD:** GitHub Actions

## Repository Structure

```
services/
  ├── rms-integration-service/    # RMS Integration Service implementation
  └── pricelogix-feed-service/    # PriceLogix Feed Service implementation
docs/         # Documentation for setup and architecture
config/       # Configuration files (Docker Compose, Prometheus, Loki)
diagrams/     # System diagrams in Mermaid format
```

## Getting Started

### Prerequisites

- **Docker** and **Docker Compose** installed
- **Java 21** installed
- **Gradle** installed for local development

### Setup Instructions

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/and1teachera/price-change-management-system.git
    cd price-change-management-system
    ```

2. **Configuration:**
    - Navigate to the `docs/` folder for detailed setup instructions
    - Configure environment variables for:
      - RMS Integration Service instance assignments
      - RabbitMQ credentials
      - TLS certificates
      - Service URLs

3. **Run the Environment:**
    ```bash
    docker-compose up
    ```

4. **Access Services:**
    - RabbitMQ Management: `http://localhost:15672`
    - Prometheus: `http://localhost:9090`
    - Grafana: `http://localhost:3000`

### Running Locally

To run services locally without Docker:

```bash
# Start an RMS Integration Service instance with specific store group assignment
./gradlew bootRun --project-dir services/rms-integration-service \
  -Pstore.group=group1

# Start the PriceLogix Feed Service
./gradlew bootRun --project-dir services/pricelogix-feed-service
```

## Monitoring and Logging

- **Metrics and Alerts:**
    - Prometheus collects metrics from all service instances and RabbitMQ
    - Grafana dashboards show:
      - Individual instance performance
      - Aggregate system metrics
      - Queue depth and processing latency
      - Error rates across instances
    - Configurable alerts for system health monitoring

- **Centralized Logging:**
    - Loki aggregates logs from all service instances
    - Logs are tagged with instance identifiers
    - Grafana provides unified view for:
      - Log filtering and analysis
      - Cross-instance correlation
      - Performance troubleshooting