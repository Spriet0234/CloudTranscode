# Cloud-Based Media Transcoding Service

A comprehensive cloud-based media transcoding and optimization service that processes videos, images, and audio files with modern web technologies.

## 🚀 Features

- **Multi-format Support**: Process videos (MP4, AVI, MOV), audio (MP3, WAV, FLAC), and images (JPEG, PNG, WebP)
- **Real-time Processing**: Live progress tracking with WebSocket updates
- **Smart Optimization**: Automatic compression and format optimization for web and mobile
- **Metadata Extraction**: Comprehensive file metadata analysis
- **Subtitle Generation**: Auto-generated subtitles for video content
- **Analytics Dashboard**: Detailed processing statistics and insights
- **RESTful API**: Complete API for integration with other services
- **Modern UI**: Beautiful, responsive React interface with Tailwind CSS

## 🏗️ Architecture

### Frontend (React + TypeScript + Tailwind CSS)
- **Upload Interface**: Drag-and-drop file upload with processing settings
- **Processing Queue**: Real-time job tracking and progress monitoring
- **Analytics Dashboard**: Statistics and insights visualization
- **Responsive Design**: Modern UI that works on all devices

### Backend (Spring Boot + Java 17)
- **RESTful APIs**: Complete set of endpoints for file processing
- **Async Processing**: Non-blocking file processing with queue management
- **Database Integration**: PostgreSQL with JPA/Hibernate
- **Security**: CORS-enabled with configurable authentication
- **Monitoring**: Health checks and metrics endpoints

### Database (PostgreSQL + Prisma)
- **Job Tracking**: Complete processing job lifecycle management
- **Metadata Storage**: Detailed file metadata and processing results
- **User Management**: User accounts and API key management
- **Analytics**: System metrics and performance tracking

## 📋 Prerequisites

- **Node.js** 18+ and npm
- **Java** 17+
- **Maven** 3.6+
- **PostgreSQL** 12+ (or Supabase account)
- **FFmpeg** (for media processing)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd media-transcoding-service
```

### 2. Database Setup
1. Create a PostgreSQL database (or use Supabase)
2. Copy `environment.template` to `.env`
3. Update the `DATABASE_URL` with your database connection string

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
The frontend will be available at `http://localhost:5173`

### 4. Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The backend API will be available at `http://localhost:8080/api`

### 5. Database Migration
```bash
# In the root directory
npx prisma generate
npx prisma db push
```

## 🔧 Configuration

### Environment Variables
Copy `environment.template` to `.env` and configure:

```env
# Database
DATABASE_URL="postgresql://username:password@host:5432/database"

# Spring Boot
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
ADMIN_PASSWORD=admin123

# Media Processing
FFMPEG_PATH=ffmpeg
TEMP_DIR=/tmp/media-processing

# Cloud Storage (Optional)
AWS_REGION=us-east-1
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
S3_BUCKET=your-media-transcoding-bucket
```

### Processing Settings
Configure media processing options:
- **Quality**: Low, Medium, High
- **Format**: Auto-detect or specific formats
- **Optimization**: Web and mobile optimization
- **Features**: Subtitle generation, metadata extraction

## 📚 API Documentation

### Upload File
```http
POST /api/media/upload
Content-Type: multipart/form-data

file: [binary file]
settings: {
  "quality": "high",
  "format": "auto",
  "optimizeForWeb": true,
  "addSubtitles": false
}
```

### Get Jobs
```http
GET /api/media/jobs?status=all&page=0&size=10
```

### Get Job Details
```http
GET /api/media/jobs/{jobId}
```

### Get Statistics
```http
GET /api/media/stats
```

### Health Check
```http
GET /api/media/health
```

## 🎯 Usage

1. **Upload Files**: Drag and drop media files or click to browse
2. **Configure Settings**: Choose quality, format, and optimization options
3. **Monitor Progress**: Track processing in real-time
4. **Download Results**: Get optimized files when processing completes
5. **View Analytics**: Monitor system performance and statistics

## 🔄 Processing Pipeline

1. **File Upload**: Secure file upload with validation
2. **Queue Management**: Intelligent job scheduling and prioritization
3. **Media Analysis**: Metadata extraction and format detection
4. **Processing**: FFmpeg-based transcoding and optimization
5. **Quality Control**: Validation and error handling
6. **Storage**: Secure file storage (local or cloud)
7. **Notification**: Real-time progress updates

## 📊 Database Schema

The application uses a comprehensive database schema with:
- **Users**: User accounts and authentication
- **Processing Jobs**: Job lifecycle and status tracking
- **File Metadata**: Detailed media file information
- **Processing Logs**: Detailed processing history
- **System Metrics**: Performance and analytics data

## 🚀 Deployment

### Docker (Coming Soon)
```bash
docker-compose up -d
```

### Manual Deployment
1. Build frontend: `npm run build`
2. Build backend: `mvn clean package`
3. Deploy JAR file to your server
4. Configure environment variables
5. Set up reverse proxy (nginx recommended)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Check the documentation
- Review the API endpoints

## 🔮 Roadmap

- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Advanced video editing features
- [ ] AI-powered content analysis
- [ ] Batch processing capabilities
- [ ] Advanced user management
- [ ] Payment integration
- [ ] CDN integration
