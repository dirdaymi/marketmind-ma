# MarketMind MA - Market Intelligence Platform

A comprehensive market intelligence platform designed to automatically detect business and IT opportunities in Morocco by analyzing public online discussions using AI and NLP techniques.

## Overview

MarketMind MA collects discussions from multiple platforms (Reddit, Hacker News, RSS feeds, etc.), processes them using Natural Language Processing (NLP), clusters similar problems using machine learning, and transforms these clusters into structured business opportunities with market analysis.

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Angular 17    │────▶│  Spring Boot 3   │────▶│  PostgreSQL 15  │
│    Frontend     │◀────│     Backend      │◀────│    + pgvector   │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │  Python FastAPI  │
                        │  NLP Service     │
                        └──────────────────┘
```

### Tech Stack

- **Frontend**: Angular 17, Angular Material, Chart.js, RxJS
- **Backend**: Spring Boot 3, Java 17, Spring Security (JWT), Spring Data JPA
- **NLP Service**: Python 3.11, FastAPI, sentence-transformers, scikit-learn (DBSCAN)
- **Database**: PostgreSQL 15 with pgvector extension
- **DevOps**: Docker, Docker Compose

## Features

### Data Collection
- Multi-source data collection (Reddit, Hacker News, RSS feeds)
- Scheduled collection with Spring Scheduler
- Automatic de-duplication via external IDs
- Keyword-based filtering for Morocco/IT/Business content

### NLP Processing
- Multilingual text processing (French, Arabic, English)
- Sentence embedding generation using transformer models
- DBSCAN clustering for grouping similar discussions
- Automatic cluster labeling and key term extraction

### Business Intelligence
- TAM/SAM/SOM market sizing calculations
- Competitive landscape analysis
- Opportunity confidence scoring
- Manual validation workflow

### Dashboard
- Real-time statistics and KPIs
- Interactive charts and visualizations
- Cluster exploration interface
- Opportunity management

## Quick Start

### Prerequisites

- Java 17+
- Node.js 20+
- Python 3.11+
- PostgreSQL 15+ with pgvector
- Docker & Docker Compose (optional)

### Option 1: Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd marketmind-ma
```

2. Start all services:
```bash
cd docker
docker-compose up -d
```

3. Access the application:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/v1
- NLP Service: http://localhost:8000

### Option 2: Manual Setup

#### 1. Database Setup

```bash
# Install PostgreSQL with pgvector
# On Ubuntu/Debian:
sudo apt-get install postgresql-15
sudo apt-get install postgresql-15-pgvector

# Create database and user
sudo -u postgres psql
create database marketmind;
create user marketmind with encrypted password 'marketmind123';
grant all privileges on database marketmind to marketmind;
\c marketmind
create extension if not exists vector;
\q

# Run initialization script
psql -U marketmind -d marketmind -f docker/init-db.sql
```

#### 2. Backend Setup

```bash
cd marketmind-backend

# Build and run
./mvnw clean package
java -jar target/marketmind-backend-1.0.0.jar

# Or using Maven
./mvnw spring-boot:run
```

The backend will be available at http://localhost:8080

#### 3. NLP Service Setup

```bash
cd marketmind-nlp

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the service
python app.py
```

The NLP service will be available at http://localhost:8000

#### 4. Frontend Setup

```bash
cd marketmind-frontend

# Install dependencies
npm install

# Run development server
ng serve
```

The frontend will be available at http://localhost:4200

## Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| analyst | analyst123 | ANALYST |

## API Documentation

Once the backend is running, API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Key Endpoints

#### Authentication
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token
- `GET /api/v1/auth/me` - Get current user

#### Dashboard
- `GET /api/v1/dashboard/stats` - Get dashboard statistics
- `GET /api/v1/dashboard/health` - Health check

#### Posts
- `GET /api/v1/posts` - List all posts
- `GET /api/v1/posts/{id}` - Get post by ID
- `GET /api/v1/posts/search?query={text}` - Search posts

#### Clusters
- `GET /api/v1/clusters` - List all clusters
- `POST /api/v1/clusters/run-clustering` - Trigger clustering

#### Opportunities
- `GET /api/v1/opportunities` - List all opportunities
- `POST /api/v1/opportunities` - Create opportunity
- `POST /api/v1/opportunities/{id}/validate` - Validate opportunity

## Project Structure

```
marketmind-ma/
├── marketmind-backend/          # Spring Boot Backend
│   ├── src/main/java/com/marketmind/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/             # Business Logic
│   │   ├── repository/          # Data Access
│   │   ├── domain/              # Entity Classes
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── security/            # JWT & Security
│   │   ├── scheduler/           # Scheduled Jobs
│   │   └── integration/         # Data Collectors
│   └── pom.xml
│
├── marketmind-frontend/         # Angular Frontend
│   ├── src/app/
│   │   ├── core/                # Services, Guards, Interceptors
│   │   ├── features/            # Page Components
│   │   ├── shared/              # Shared Components
│   │   └── models/              # TypeScript Models
│   └── package.json
│
├── marketmind-nlp/              # Python NLP Service
│   ├── services/                # NLP Services
│   ├── models/                  # ML Models
│   └── app.py                   # FastAPI Application
│
├── docker/                      # Docker Configuration
│   ├── docker-compose.yml
│   └── init-db.sql              # Database Schema
│
└── README.md
```

## Data Sources

The platform collects data from multiple sources:

1. **Reddit** (r/morocco, r/Casablanca, r/MoroccoTech, r/startups)
2. **Hacker News** (Morocco/Africa related posts)
3. **RSS Feeds** (Moroccan tech news)
4. **Medium** (startup/Morocco tags)

### Adding New Sources

To add a new data source:

1. Create a new collector class implementing `DataCollector` interface:

```java
@Component
public class NewSourceCollector implements DataCollector {
    
    @Override
    public void collect() {
        // Implementation
    }
    
    @Override
    public PostSource getSource() {
        return PostSource.NEW_SOURCE;
    }
}
```

2. Register in `DataCollectionScheduler`

## NLP Pipeline

1. **Text Cleaning**: Remove URLs, emails, HTML tags, normalize text
2. **Language Detection**: Identify language (FR/AR/EN)
3. **Embedding Generation**: Convert text to 384-dimensional vectors
4. **Clustering**: DBSCAN algorithm groups similar posts
5. **Cluster Labeling**: Extract key terms for cluster naming

## Configuration

### Backend (application.properties)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/marketmind
spring.datasource.username=marketmind
spring.datasource.password=marketmind123

# JWT
marketmind.jwt.secret=your-secret-key
marketmind.jwt.expiration=86400000

# NLP Service
marketmind.nlp.service.url=http://localhost:8000

# Data Collectors
marketmind.collectors.reddit.enabled=true
marketmind.collectors.hackernews.enabled=true
```

### NLP Service (.env)

```env
HOST=0.0.0.0
PORT=8000
MODEL_NAME=sentence-transformers/all-MiniLM-L6-v2
```

## Development

### Running Tests

**Backend:**
```bash
cd marketmind-backend
./mvnw test
```

**Frontend:**
```bash
cd marketmind-frontend
ng test
```

**NLP Service:**
```bash
cd marketmind-nlp
pytest
```

### Code Style

- **Java**: Follow Google Java Style Guide
- **TypeScript**: Follow Angular Style Guide
- **Python**: Follow PEP 8

## Deployment

### Production Considerations

1. **Security**:
   - Change default passwords
   - Use strong JWT secret
   - Enable HTTPS
   - Configure CORS properly

2. **Performance**:
   - Enable database connection pooling
   - Configure Redis for caching (optional)
   - Use CDN for static assets

3. **Monitoring**:
   - Set up application metrics
   - Configure log aggregation
   - Enable health checks

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MARKETMIND_JWT_SECRET` | JWT signing secret | auto-generated |
| `SPRING_DATASOURCE_URL` | Database URL | jdbc:postgresql://localhost:5432/marketmind |
| `MARKETMIND_NLP_SERVICE_URL` | NLP service URL | http://localhost:8000 |

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Acknowledgments

- Sentence Transformers library for embeddings
- Spring Boot team for the excellent framework
- Angular team for the frontend framework
- PostgreSQL pgvector extension for vector similarity search

## Support

For support, email support@marketmind.ma or create an issue in the repository.

## Roadmap

- [ ] Twitter/X integration
- [ ] Advanced sentiment analysis
- [ ] Real-time notifications
- [ ] Mobile application
- [ ] Advanced analytics dashboard
- [ ] Multi-language support improvements
- [ ] Machine learning model fine-tuning

---

**MarketMind MA** - Empowering entrepreneurs and analysts with AI-driven market intelligence for Morocco.
