# Media Transcoding Service - Backend

Spring Boot backend service for the cloud-based media transcoding application.

## Features

- **RESTful API** for file upload and processing management
- **PostgreSQL database** integration with JPA/Hibernate
- **File upload support** up to 500MB
- **Processing job queue** management
- **System statistics** and monitoring endpoints
- **CORS enabled** for frontend integration

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (optional - can run with H2 for development)

## Quick Start

### 1. Database Setup (Optional)

If you want to use PostgreSQL:

```bash
# Create database
psql -U postgres
CREATE DATABASE media_transcoding;
```

### 2. Run the Application

```bash
# Compile and run
mvn spring-boot:run

# Or compile and run JAR
mvn clean package
java -jar target/media-transcoding-service-1.0.0.jar
```

The application will start on `http://localhost:8080`

### 3. Test the API

Check if the service is running:
```bash
curl http://localhost:8080/api/media/health
```

## API Endpoints

### File Upload
- `POST /api/media/upload` - Upload file for processing
  - Parameters: `file` (multipart), `settings` (JSON string)

### Job Management
- `GET /api/media/processing-jobs` - Get all processing jobs
- `GET /api/media/jobs/{jobId}` - Get specific job details
- `DELETE /api/media/processing-jobs/{jobId}` - Delete a job

### System Stats
- `GET /api/media/system-stats` - Get system statistics and metrics
- `GET /api/media/health` - Health check endpoint

## Configuration

The application can be configured via `application.yml`:

- **Database**: PostgreSQL connection settings
- **File Upload**: Max file size limits
- **CORS**: Frontend URL allowlist
- **Logging**: Debug levels and formats

## Development

### Database Mode

The application uses Hibernate's `ddl-auto: update` which will automatically create tables on startup.

For production, change this to `validate` and use database migrations.

### Mock Data

Currently, the service returns mock data for:
- System statistics
- Processing jobs (until real FFmpeg integration)
- File processing (files are uploaded but not actually processed yet)

## Next Steps

1. **FFmpeg Integration** - Add actual media processing capabilities
2. **Queue System** - Implement Redis/RabbitMQ for job queuing
3. **Cloud Storage** - Add S3 integration for file storage
4. **Authentication** - Add JWT token-based authentication
5. **Real Metrics** - Integrate with monitoring tools like Micrometer/Prometheus

## Architecture

```
├── controller/     # REST API controllers
├── service/        # Business logic services
├── repository/     # JPA repositories
├── entity/         # Database entities
├── dto/            # Data Transfer Objects
├── config/         # Configuration classes
└── resources/      # Application configuration
``` 