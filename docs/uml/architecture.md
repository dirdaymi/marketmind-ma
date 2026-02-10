# MarketMind MA - Architecture Documentation

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Angular 17 Frontend                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │  Dashboard  │  │   Posts     │  │  Clusters   │  │Opportunities│ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTP/REST + JWT
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           APPLICATION LAYER                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                  Spring Boot 3 Backend                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │   REST API  │  │   Security  │  │   Services  │  │Schedulers  │ │   │
│  │  │  Controllers│  │    (JWT)    │  │             │  │            │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ JPA / JDBC
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                                       │
│  ┌─────────────────────────┐              ┌─────────────────────────────┐   │
│  │    PostgreSQL 15        │              │      pgvector Extension     │   │
│  │  ┌─────────────────┐    │              │  ┌─────────────────────┐    │   │
│  │  │  Raw Posts      │    │              │  │  Vector Storage     │    │   │
│  │  │  Clusters       │    │              │  │  Similarity Search  │    │   │
│  │  │  Opportunities  │    │              │  │  K-NN Queries       │    │   │
│  │  │  Users          │    │              │  └─────────────────────┘    │   │
│  │  └─────────────────┘    │              └─────────────────────────────┘   │
│  └─────────────────────────┘                                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTP/REST
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            AI/ML LAYER                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Python FastAPI NLP Service                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │   │
│  │  │ Embeddings  │  │  Clustering │  │    Text     │  │  Language  │ │   │
│  │  │  Service    │  │   (DBSCAN)  │  │ Processing  │  │  Detection │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     MarketMind MA System                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────┐        ┌─────────────────────┐         │
│  │   Web Frontend      │        │   Data Collectors   │         │
│  │   (Angular 17)      │        │   (Spring Boot)     │         │
│  │                     │        │                     │         │
│  │  • Dashboard        │        │  • Reddit Collector │         │
│  │  • Post Explorer    │        │  • HN Collector     │         │
│  │  • Cluster View     │        │  • RSS Collector    │         │
│  │  • Opportunity Mgmt │        │  • Medium Collector │         │
│  └──────────┬──────────┘        └──────────┬──────────┘         │
│             │                               │                    │
│             │ HTTP/REST                     │ Store              │
│             ▼                               ▼                    │
│  ┌─────────────────────┐        ┌─────────────────────┐         │
│  │   API Gateway       │        │   PostgreSQL DB     │         │
│  │   (Spring Boot)     │◀──────▶│   (pgvector)        │         │
│  │                     │  JPA   │                     │         │
│  │  • Auth Controller  │        │  • raw_posts        │         │
│  │  • Post Controller  │        │  • embeddings       │         │
│  │  • Cluster Ctrl     │        │  • clusters         │         │
│  │  • Opportunity Ctrl │        │  • opportunities    │         │
│  └──────────┬──────────┘        └─────────────────────┘         │
│             │                                                    │
│             │ HTTP/REST                                          │
│             ▼                                                    │
│  ┌─────────────────────┐                                         │
│  │   NLP Service       │                                         │
│  │   (FastAPI)         │                                         │
│  │                     │                                         │
│  │  • Embedding Gen    │                                         │
│  │  • DBSCAN Cluster   │                                         │
│  │  • Text Analysis    │                                         │
│  └─────────────────────┘                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

### Collection Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Reddit    │    │  Hacker     │    │    RSS      │    │   Medium    │
│     API     │    │    News     │    │   Feeds     │    │     API     │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │                  │
       └──────────────────┴──────────────────┴──────────────────┘
                          │
                          ▼
              ┌─────────────────────┐
              │  Data Collectors    │
              │  (Spring Scheduler) │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │   Deduplication     │
              │   (external_id)     │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │   PostgreSQL        │
              │   raw_posts table   │
              └─────────────────────┘
```

### Processing Flow

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Raw Posts         │───▶│   Text Cleaning     │───▶│   Embedding Gen     │
│   (PostgreSQL)      │    │   (NLP Service)     │    │   (NLP Service)     │
└─────────────────────┘    └─────────────────────┘    └──────────┬──────────┘
                                                                   │
                                                                   ▼
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Clusters          │◀───│   DBSCAN Clustering │◀───│   Vector Storage    │
│   (PostgreSQL)      │    │   (NLP Service)     │    │   (pgvector)        │
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘
```

### Opportunity Flow

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Clusters          │───▶│   Opportunity       │───▶│   Market Analysis   │
│   (PostgreSQL)      │    │   Generation        │    │   (TAM/SAM/SOM)     │
└─────────────────────┘    └─────────────────────┘    └──────────┬──────────┘
                                                                   │
                                                                   ▼
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Dashboard         │◀───│   Opportunities     │◀───│   Validation        │
│   (Angular)         │    │   (PostgreSQL)      │    │   (Analyst Review)  │
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Docker Compose                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   nginx     │  │   Spring    │  │   Python    │             │
│  │  (Angular)  │  │   Boot      │  │   FastAPI   │             │
│  │   :4200     │  │   :8080     │  │   :8000     │             │
│  └──────┬──────┘  └──────┬──────┘  └─────────────┘             │
│         │                │                                      │
│         └────────────────┘                                      │
│                   │                                              │
│                   ▼                                              │
│          ┌─────────────┐                                        │
│          │ PostgreSQL  │                                        │
│          │  :5432      │                                        │
│          │ + pgvector  │                                        │
│          └─────────────┘                                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Security Layers                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Layer 1: Transport Security                                     │
│  ├── HTTPS/TLS for all communications                            │
│  └── Certificate-based authentication (optional)                 │
│                                                                  │
│  Layer 2: Authentication                                         │
│  ├── JWT (JSON Web Tokens)                                       │
│  ├── Token expiration: 24 hours                                  │
│  └── Refresh token mechanism                                     │
│                                                                  │
│  Layer 3: Authorization                                          │
│  ├── Role-Based Access Control (RBAC)                            │
│  ├── Roles: ADMIN, ANALYST, VIEWER                               │
│  └── Method-level security with @PreAuthorize                    │
│                                                                  │
│  Layer 4: Input Validation                                       │
│  ├── Bean Validation (JSR-380)                                   │
│  ├── SQL Injection prevention (JPA)                              │
│  └── XSS protection                                              │
│                                                                  │
│  Layer 5: Audit Logging                                          │
│  ├── User actions tracking                                       │
│  └── Data changes history                                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Scalability Considerations

### Horizontal Scaling

```
                    ┌─────────────┐
                    │   Load      │
                    │  Balancer   │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  Backend    │ │  Backend    │ │  Backend    │
    │  Instance 1 │ │  Instance 2 │ │  Instance N │
    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
                    ┌──────┴──────┐
                    │  PostgreSQL │
                    │   Primary   │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │  PostgreSQL │
                    │   Replica   │
                    └─────────────┘
```

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| Frontend | Angular 17 | User Interface |
| Frontend | Angular Material | UI Components |
| Frontend | Chart.js | Data Visualization |
| Backend | Spring Boot 3 | REST API |
| Backend | Spring Security | Authentication/Authorization |
| Backend | Spring Data JPA | Data Access |
| AI/ML | Python FastAPI | NLP Service |
| AI/ML | sentence-transformers | Text Embeddings |
| AI/ML | scikit-learn | Clustering |
| Database | PostgreSQL 15 | Primary Database |
| Database | pgvector | Vector Storage |
| DevOps | Docker | Containerization |
| DevOps | Docker Compose | Local Orchestration |
