# SwiftEats Development Journey

## Development Prompts and Decision Log

### Initial Project Planning and Setup

- **User Prompt**: "I need to build a food delivery platform like UberEats with a microservices architecture."
- **User Prompt**: "What architecture would be best for a startup that needs to move fast but maintain scalability?"
- **Decision Point**: Chose modular monolith for initial development with clear boundaries for future microservices split
- **User Prompt**: "Create a directory structure for the project that separates the different service components."
- **Implementation**: Created directory structure with separate modules for order, restaurant, driver services, and API gateway

### Technology Selection

- **User Prompt**: "What database would be best for our food delivery platform? We need reliability and JSON support."
- **Decision Point**: Selected PostgreSQL for its reliability, ACID compliance, and JSON capabilities
- **User Prompt**: "We need a message broker for communication between services. What are the options?"
- **Decision Point**: Selected RabbitMQ for its reliability and simplicity, sufficient for the current scale requirements
- **User Prompt**: "How can we ensure menu browsing is super fast for customers?"
- **Decision Point**: Selected Redis for caching with its speed, pub/sub capabilities, and Spring Boot integration

### Architecture Design

- **User Prompt**: "How should we handle real-time driver location updates efficiently?"
- **Decision Point**: Redis pub/sub for real-time updates + WebSocket for client notifications
- **User Prompt**: "What's the best way to ensure orders aren't lost during processing?"
- **Decision Point**: Queue-based processing with RabbitMQ + circuit breakers for dependent services
- **User Prompt**: "Our requirement is menu browsing in under 200ms. How do we achieve this?"
- **Decision Point**: Aggressive caching with Redis + database query optimization

### API Design

- **User Prompt**: "Create an API specification for our platform that follows best practices."
- **User Prompt**: "What's the best way to version our APIs for future changes?"
- **Decision Point**: Path-based versioning for simplicity and client compatibility

### Project Documentation

- **User Prompt**: "We need comprehensive documentation for the project. What should it include?"
- **User Prompt**: "Create a detailed README.md for the project."
- **Decision Point**: Created comprehensive documentation to facilitate onboarding and maintenance

### Docker and Infrastructure Setup

- **User Prompt**: "Help me set up Docker for local development and testing."
- **Decision Point**: Docker Compose for easy local development and testing
- **User Prompt**: "How should we initialize the database schemas for different services?"
- **Decision Point**: Startup script to create separate schemas for service isolation

### Service Implementation

- **User Prompt**: "Create a readme.md file for this and mention all the APIs endpoints with curl request."
- **Implementation**: Created comprehensive README files for the Simulator Service and API Gateway with API documentation
- **User Prompt**: "Can you test if the simulator service endpoints are working?"
- **Implementation**: Tested simulator service endpoints and identified issues
- **User Prompt**: "Let's check if the simulator service has any error logs."
- **Discovery**: Found 405 Method Not Allowed errors when sending location updates
- **User Prompt**: "What could be causing these 405 errors?"
- **Issue**: Identified mismatch between data format sent by simulator and expected by driver service

### Debugging and Fixing

- **User Prompt**: "Can we fix the simulator service to work with the driver service?"
- **Solution**: Updated LocationUpdate model to use Long driverId instead of String UUID
- **User Prompt**: "Is it working by calling api gateway?"
- **Decision Point**: Modified simulator service to route requests through API Gateway
- **User Prompt**: "Can you create readme.md file for simulator service with all working api?"
- **Implementation**: Created detailed documentation of simulator service APIs, configuration options, and troubleshooting tips

### Repository Management

- **User Prompt**: "Do you want to add more in .gitignore?"
- **Implementation**: Enhanced .gitignore file with comprehensive patterns for Spring Boot microservices
- **User Prompt**: "Do you want to update CHAT_HISTORY.md file?"
- **Implementation**: Reformatted chat history to show user prompts instead of AI responses

### Testing and Quality Assurance

- **User Prompt**: "Can you write comprehensive test cases that covers all scenarios for each of the services and also update the test coverage report for these?"
- **Implementation**: Created extensive test suites for all services with integration, unit and end-to-end tests
- **Decision Point**: Implemented test coverage reporting using JaCoCo
- **Implementation**: Set up automatic test coverage threshold enforcement to maintain quality
