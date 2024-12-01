# RMS to PriceLogix Integration System

## Overview

This system facilitates seamless integration between the **Retail Management Suite (RMS)** and the **PriceLogix Enterprise Optimizer**. It processes retail price adjustments and provides actionable insights to optimize pricing decisions, focusing on:

- **Price Adjustment Schedules (PAS):** Fetches and processes schedule data for planned price changes.
- **Price Adjustment Directives (PAD) and Price Restoration Actions (PRA):** Processes clearance and pricing adjustments in line with business requirements.
- **Queue-Based Communication:** Enables reliable, asynchronous data exchange using RabbitMQ.

## Features

- **Data Integration:** Processes and transforms data from multiple sources into a standardized format for PriceLogix.
- **Queue-Based Architecture:** RabbitMQ facilitates efficient message routing and error handling.
- **Monitoring:** Integrated Prometheus and Grafana provide visibility into system health and performance.
- **Resilience:** Dead Letter Queues (DLQs) and robust error handling ensure reliability during processing.

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
services/     # Source code for individual services
docs/         # Documentation for setup and architecture
config/       # Configuration files (Docker Compose, Prometheus, Loki)
diagrams/     # System diagrams in Mermaid format
```

## Getting Started

### Prerequisites

- **Docker** and **Docker Compose** installed.
- **Java 21** installed.
- **Gradle** installed for local development.

### Setup Instructions

1. **Clone the Repository:**
    
    ```bash
    git clone https://github.com/and1teachera/price-change-management-system.git
    cd price-change-management-system
    ```
    
2. **Configuration:**
    
    - Navigate to the `docs/` folder for detailed setup instructions.
    - Set up environment variables for RabbitMQ credentials, TLS certificates, and service URLs.
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

- Build and start the required services using Gradle:
    
    ```bash
    ./gradlew bootRun --project-dir services/pad-processing
    ```
    

## Monitoring and Logging

- **Metrics and Alerts:**
    - Prometheus collects metrics from RabbitMQ and application endpoints.
    - Grafana visualizes metrics, with alerts set for queue depth, processing latency, and error rates.
- **Centralized Logging:**
    - Loki aggregates logs from all services.
    - Logs are accessible via Grafana for filtering, analysis, and correlation with metrics.
