# Virtual-Power-Plant

## Build Instructions

Battery Management System
Overview
This project implements a Battery Management System using Spring Boot. 
The system provides REST APIs to manage battery data, including adding new batteries, querying batteries by postcode range, 
and retrieving statistics. The implementation includes advanced engineering concepts such as concurrency handling, 
rate-limiting, JWT-based authentication, real-time data streaming, caching, and more.

Features
1. REST API in Spring Boot
2. Endpoint to accept a list of batteries and persist them in a database
3. Endpoint to query batteries by postcode range (as well as battery capacity range) and return statistics
4. Usage of Java Streams
5. Unit tests and Integration tests.
6. Concurrency handling for large number of battery registrations
7. Rate-limiting to prevent misuse
8. Filtering based on minimum or maximum watt capacity
9. Endpoint to identify batteries below a certain capacity threshold
10. JWT-based authentication
11. Real-time data streaming using RabbitMQ
12. Endpoint for real-time stats on aggregated battery capacities
13. Logging framework integration
14. Health checks and metrics endpoints
15. Caching using Redis
16. Microservices architecture
18. CI/CD pipeline for automated testing and deployment
19. Containerization using Docker
20. Kubernetes manifests for deployment
21. API documentation using Swagger

Getting Started
Prerequisites
•  Java 17
•  Maven
•  Docker
•  Kubernetes (Minikube or any other Kubernetes cluster)

Building the Project
1. Clone the repository:

```
git clone https://github.com/Mazhar30/Virtual-Power-Plant.git
cd Virtual-Power-Plant
```

2. Before building the project, ensure that the required log directory exists:
```bash
mkdir -p /hms/logs/vpp
```
Then proceed with the build:
Build the project using Maven:
```
mvn clean install
```

Running the Application

Using Docker Compose:
Build and start the services using Docker Compose:
```
docker-compose up --build
```

Using Kubernetes

1. Push Docker images to Docker Hub: (I have already pushed the image to docker hub, you can skip this point if you want)
```
docker build -t your-dockerhub-username/battery-management-service:latest ./cc
docker push your-dockerhub-username/battery-management-service:latest

docker build -t your-dockerhub-username/battery-update-service:latest ./bu
docker push your-dockerhub-username/battery-update-service:latest
```
2. Apply Kubernetes manifests: (If you push new image, please make sure to update the image in 
       k8s/battery-management-service/deployment.yaml and k8s/battery-update-service/deployment.yaml)
```
kubectl apply -f k8s/battery-management-service/
kubectl apply -f k8s/battery-update-service/
kubectl apply -f k8s/mongo/
kubectl apply -f k8s/rabbitmq/
kubectl apply -f k8s/redis/
```

## API Documentation
The API documentation is available via Swagger. Once the application is running, you can access the Swagger UI at:

http://localhost:8080/swagger-ui/

You can find the API curl call inside curl/ directory.

# Endpoints
•  Authenticate User: POST /api/auth/login

•  Add Batteries: POST /api/batteries/save  (Needs Authentication)

•  Get Batteries by Postcode Range (along with capacity range): GET /api/batteries/getBatteries (Needs Authentication)

•  Get Batteries Below Capacity: GET /api/batteries/getBatteriesBelowCapacity (Needs Authentication)

•  Real-time Stats: GET /api/batteries/stats  (Needs Authentication)

•  Health Check: GET /actuator/health (Open in browser)

•  Metrics: GET /actuator/metrics (Open in browser)

•  Get Real-time battery streaming data: GET /api/streaming/batteryUpdates (open the test-sse.html and click the button, 
                                                                            In the browser network tab you can see the streaming.)

# Configuration
•  Logging: Configured in logback.xml with logs stored in ./logs/

•  JWT Authentication: Configured in application.yml.

•  Rate Limiting: Configured using Spring Boot's built-in support and application.yml.

# Testing
Run unit tests using Maven:
```
mvn test
```
# CI/CD Pipeline
The project includes a GitHub Actions workflow for CI/CD. The workflow is defined in .github/workflows/ci-cd.yml.
