# Architecture Documentation

## Overview

This document explains the architecture of the RMS to PriceLogix Integration System, which implements a distributed event-driven architecture to process retail price adjustments across stores, focusing on scalability, reliability and data consistency.

## Architectural Style

The architecture is designed around an event-driven model with distributed processing, providing:
1. **Scalable Processing**: Multiple RMS Integration Service instances store groups in parallel.
2. **Loose Coupling**: Services communicate through RabbitMQ, making it easy to update them independently.
3. **Reliable Message Processing**: Asynchronous messaging ensures data reliability during high load.
4. **Maintainable Components**: Clear separation of concerns between services facilitates independent testing and deployment.

## Key Components

### RMS Integration Service
- Retrieves price adjustment schedules via REST API for specific store group
- Transforms schedule data into a standard message format
- Publishes processed data to a shared RabbitMQ exchange
- Maintains its own state and schedule

### PriceLogix Feed Service
A single instance of this service acts as the centralized consumer, processing messages from all RMS Integration Service instances. It:
- Consumes messages from the shared RabbitMQ
- Processes price adjustments sequentially
- Manages data transformation and validation
- Generates output for PriceLogix

### Message Queue Layer (RabbitMQ)
- Acts as the message broker.
- Manages message delivery, persistence, and retries.
- Uses dead letter queues for failed messages
- Message routing and retry logic
## Message Flow and Processing

The system implements a coordinated message flow pattern that ensures reliable processing across distributed components:

### Publisher-Consumer Pattern
RMS Integration Service instances act as publishers, sending messages to a shared RabbitMQ exchange. The PriceLogix Feed Service operates as the single consumer, processing messages sequentially. This pattern ensures:

1. Message ordering is maintained regardless of source instance
2. Processing capacity scales with additional RMS Integration Service instances
3. Data consistency is preserved through centralized consumption
4. System load is distributed across store groups

### Instance Coordination
The distributed nature of RMS Integration Service instances requires careful coordination:
- **Store Group Assignment**: Each instance is assigned specific stores to avoid duplicate processing.
- **Schedule Management**: Instances manage their own schedules but share monitoring, logging, and infrastructure.

## Architectural Decisions
### Distribution Strategy
- **Multiple Instances**: Allows for parallel store group processing and better system resilience.   
### Message Queue Architecture
- **RabbitMQ**: Chosen for reliable message delivery, persistence, and error handling.
### Data Consistency
- Each store group is processed by a single instance
- Message order is preserved
- Clear transaction boundaries ensure consistency
#### Format-specific validation rules:
  * PSE Format:
    - Mandatory field presence validation
    - Date sequence integrity checks
    - Fiscal year format verification
    - Event type code validation
    - Cross-field logical validation
  * PAR Format:
    - Numeric field range validation
    - Location key existence verification
    - Status code transformation validation
    - Date format standardization
    - Price and percentage range checks
#### Transformation requirements:
  * PSE Transformation:
    - JSON to pipe-delimited conversion
    - Date field standardization
    - Event type mapping
    - Fiscal period alignment
  * PAR Transformation:
    - Status code mapping
    - Location hierarchy expansion
    - Event identifier extraction
    - Price calculation validation
### Monitoring and Observability
- Metrics collection, log aggregation, and centralized alerting support system health and performance tracking.

## Logical Components

### Core Components

- **RMS Integration Service**: Handles schedule retrieval, data transformation, and message publishing.    
- **PriceLogix Feed Service**: Processes messages and manages output generation.    

### Cross-Cutting Components
- **Monitoring Framework**: Tracks metrics, logs, health checks, and alerts.    
- **Security Framework**: Implements TLS, authentication, authorization, and audit logging.    

## Development and Testing

### Development Approach
- **Instance Isolation**: Manage state carefully for distributed components.    
- **Configuration**: Support multiple deployment environments.    

### Testing Strategy
- Integration and performance testing to verify message flow, error handling, and scalability.   

### Deployment Considerations
- Container orchestration and configuration management ensure smooth deployment and scaling.   

## Implementation Guidelines
### Service Implementation
- RMS Integration Service should handle its own configuration and support independent scaling.    
- PriceLogix Feed Service should maintain message order and process messages sequentially.   

### Message Processing
- Preserve message order and support transaction tracking and error handling.    

## Operational Architecture
### System Monitoring and Management
- **Instance View**: Tracks service behavior and resource use.    
- **System View**: Shows overall health and performance.    
- **Business View**: Tracks key metrics like price adjustment success.  

### Operational Processes
- **Configuration Management**: Centralized settings with instance-specific overrides.    
- **Deployment Management**: Supports rolling updates and compatibility.    
- **Incident Management**: Tools for quick problem identification and resolution.   

## System Evolution

### Scaling Considerations
- Supports horizontal scaling by adding service instances.    
- Message queue architecture adapts to changing publishing instances. 
### Feature and Technology Evolution
- Modular design allows for adding features without system-wide changes.    
- Standard protocols and interfaces reduce technology lock-in.  
### Maintenance and Updates
- Instance-specific maintenance minimizes system disruption.    
- Supports rolling deployments and independent component updates.  

## Security Architecture

### Security Layers
- **Transport Security**: TLS for all communication.    
- **Authentication and Authorization**: Centralized identity management with fine-grained access control.    
- **Audit and Compliance**: Centralized logging for security monitoring and reporting.
### Authentication Patterns:
- OAuth 2.0 client credentials flow for RMS API access
    - Secure credential storage in environment variables
    - Automatic token refresh handling
    - Separate credentials per environment
- File-based integration security through filesystem permissions and encryption