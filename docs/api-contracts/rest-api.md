# MarketMind MA - REST API Contracts

## Base URL

```
Development: http://localhost:8080/api/v1
Production: https://api.marketmind.ma/api/v1
```

## Authentication

All endpoints (except `/auth/**`) require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Response Format

All responses follow a standard format:

### Success Response
```json
{
  "data": { ... },
  "message": "Success",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": { ... }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Authentication Endpoints

### POST /auth/login
Authenticate user and receive JWT token.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@marketmind.ma",
  "roles": ["ADMIN", "ANALYST", "VIEWER"],
  "expiresIn": 86400000
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials

---

### POST /auth/refresh
Refresh JWT token.

**Request:**
```
Authorization: Bearer <current_token>
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@marketmind.ma",
  "roles": ["ADMIN", "ANALYST", "VIEWER"],
  "expiresIn": 86400000
}
```

---

### GET /auth/me
Get current user information.

**Response (200):**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@marketmind.ma",
  "firstName": "System",
  "lastName": "Administrator",
  "roles": ["ADMIN", "ANALYST", "VIEWER"]
}
```

---

## Dashboard Endpoints

### GET /dashboard/stats
Get dashboard statistics.

**Response (200):**
```json
{
  "totalPosts": 15420,
  "postsLast24Hours": 156,
  "postsLast7Days": 1245,
  "postsLast30Days": 4890,
  "postsBySource": {
    "REDDIT": 8500,
    "HACKER_NEWS": 3200,
    "RSS_FEED": 2720,
    "MEDIUM": 1000
  },
  "postsByStatus": {
    "RAW": 5000,
    "PROCESSED": 4000,
    "CLUSTERED": 3000,
    "ANALYZED": 2420,
    "ARCHIVED": 1000
  },
  "totalClusters": 245,
  "activeClusters": 198,
  "averagePostsPerCluster": 15.5,
  "totalOpportunities": 87,
  "opportunitiesByStatusDraft": 45,
  "opportunitiesByStatusValidated": 25,
  "opportunitiesByStatusInProgress": 12,
  "opportunitiesByStatusImplemented": 5,
  "averageOpportunityScore": 72.5,
  "averageConfidenceScore": 68.3,
  "averageMarketPotentialScore": 75.2,
  "averageFeasibilityScore": 74.1,
  "totalCollectionJobs": 1240,
  "totalPostsCollected": 25000,
  "totalPostsNew": 15420
}
```

---

### GET /dashboard/health
Health check endpoint.

**Response (200):**
```json
{
  "status": "UP",
  "service": "MarketMind MA Backend",
  "version": "1.0.0"
}
```

---

## Posts Endpoints

### GET /posts
Get all posts with pagination.

**Query Parameters:**
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 20)
- `sortBy` (string, optional): Sort field (default: collectedAt)
- `direction` (string, optional): Sort direction (asc/desc, default: desc)

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "externalId": "reddit_abc123",
      "source": "REDDIT",
      "sourceUrl": "https://reddit.com/r/morocco/comments/abc123",
      "title": "Looking for tech co-founders in Casablanca",
      "content": "I'm looking for tech co-founders in Casablanca for a fintech startup...",
      "author": "user123",
      "language": "en",
      "postedAt": "2024-01-15T08:00:00Z",
      "collectedAt": "2024-01-15T10:00:00Z",
      "status": "RAW",
      "keywords": ["startup", "casablanca", "fintech", "co-founder"],
      "metadata": {
        "subreddit": "morocco",
        "score": 45,
        "num_comments": 23
      },
      "createdAt": "2024-01-15T10:00:00Z",
      "updatedAt": "2024-01-15T10:00:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 15420,
  "totalPages": 772,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### GET /posts/{id}
Get post by ID.

**Response (200):**
```json
{
  "id": 1,
  "externalId": "reddit_abc123",
  "source": "REDDIT",
  "sourceUrl": "https://reddit.com/r/morocco/comments/abc123",
  "title": "Looking for tech co-founders in Casablanca",
  "content": "I'm looking for tech co-founders in Casablanca for a fintech startup...",
  "author": "user123",
  "language": "en",
  "postedAt": "2024-01-15T08:00:00Z",
  "collectedAt": "2024-01-15T10:00:00Z",
  "status": "RAW",
  "keywords": ["startup", "casablanca", "fintech", "co-founder"],
  "metadata": {
    "subreddit": "morocco",
    "score": 45,
    "num_comments": 23
  },
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Post not found

---

### GET /posts/search
Search posts by text.

**Query Parameters:**
- `query` (string, required): Search query

**Response (200):**
```json
[
  {
    "id": 1,
    "externalId": "reddit_abc123",
    "source": "REDDIT",
    "title": "Looking for tech co-founders in Casablanca",
    "content": "I'm looking for tech co-founders in Casablanca for a fintech startup...",
    "status": "RAW",
    "createdAt": "2024-01-15T10:00:00Z"
  }
]
```

---

### GET /posts/stats/by-source
Get post count by source.

**Response (200):**
```json
{
  "REDDIT": 8500,
  "HACKER_NEWS": 3200,
  "RSS_FEED": 2720,
  "MEDIUM": 1000
}
```

---

## Clusters Endpoints

### GET /clusters
Get all clusters with pagination.

**Query Parameters:**
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 20)
- `activeOnly` (boolean, optional): Show only active clusters (default: true)

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "clusterLabel": "CLUSTER_1",
      "name": "fintech startup casablanca",
      "description": "Cluster related to: fintech, startup, casablanca",
      "postCount": 45,
      "avgConfidence": 0.85,
      "keyTerms": ["fintech", "startup", "casablanca", "funding", "investment"],
      "representativePosts": [
        {
          "id": 1,
          "title": "Looking for tech co-founders in Casablanca",
          "content": "I'm looking for tech co-founders...",
          "source": "REDDIT"
        }
      ],
      "createdAt": "2024-01-10T10:00:00Z",
      "updatedAt": "2024-01-15T10:00:00Z",
      "isActive": true,
      "opportunityCount": 3,
      "maxOpportunityScore": 85.5
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 245,
  "totalPages": 13,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### GET /clusters/{id}
Get cluster by ID.

**Response (200):**
```json
{
  "id": 1,
  "clusterLabel": "CLUSTER_1",
  "name": "fintech startup casablanca",
  "description": "Cluster related to: fintech, startup, casablanca",
  "postCount": 45,
  "avgConfidence": 0.85,
  "keyTerms": ["fintech", "startup", "casablanca", "funding", "investment"],
  "representativePosts": [
    {
      "id": 1,
      "title": "Looking for tech co-founders in Casablanca",
      "content": "I'm looking for tech co-founders...",
      "source": "REDDIT"
    }
  ],
  "dbscanParams": {
    "eps": 0.5,
    "minSamples": 5
  },
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z",
  "isActive": true,
  "opportunityCount": 3,
  "maxOpportunityScore": 85.5
}
```

---

### POST /clusters/run-clustering
Trigger clustering process.

**Authorization:** ADMIN only

**Response (200):**
```json
{
  "message": "Clustering process started"
}
```

---

## Opportunities Endpoints

### GET /opportunities
Get all opportunities with pagination.

**Query Parameters:**
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 20)
- `sortBy` (string, optional): Sort field (default: createdAt)
- `direction` (string, optional): Sort direction (default: desc)

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "clusterId": 1,
      "clusterName": "fintech startup casablanca",
      "title": "Fintech Co-founder Matching Platform",
      "description": "A platform to connect entrepreneurs with potential co-founders in Morocco's fintech sector.",
      "problemStatement": "Difficulty finding qualified tech co-founders in Morocco",
      "proposedSolution": "Create a dedicated platform for co-founder matching with verified profiles",
      "tamSize": 500000000,
      "tamCurrency": "MAD",
      "samSize": 50000000,
      "samCurrency": "MAD",
      "somSize": 5000000,
      "somCurrency": "MAD",
      "marketAssumptions": {
        "targetUsers": 10000,
        "conversionRate": 0.05,
        "averageRevenue": 500
      },
      "competitors": [
        {
          "name": "LinkedIn",
          "description": "General professional networking",
          "strength": "Large user base"
        }
      ],
      "competitiveAdvantage": "Focus on Moroccan market, local context understanding",
      "confidenceScore": 75.0,
      "marketPotentialScore": 80.0,
      "feasibilityScore": 70.0,
      "overallScore": 75.0,
      "status": "VALIDATED",
      "priority": "HIGH",
      "validatedBy": "analyst",
      "validatedAt": "2024-01-15T10:00:00Z",
      "validationNotes": "Strong market need identified",
      "tags": ["fintech", "platform", "networking"],
      "category": "Marketplace",
      "targetAudience": "Entrepreneurs and tech professionals in Morocco",
      "createdAt": "2024-01-10T10:00:00Z",
      "updatedAt": "2024-01-15T10:00:00Z",
      "createdBy": "system"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 87,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### GET /opportunities/{id}
Get opportunity by ID.

**Response (200):** Same as single item in list response

---

### POST /opportunities
Create a new opportunity.

**Authorization:** ANALYST, ADMIN

**Request:**
```json
{
  "clusterId": 1,
  "title": "Fintech Co-founder Matching Platform",
  "description": "A platform to connect entrepreneurs with potential co-founders in Morocco's fintech sector.",
  "problemStatement": "Difficulty finding qualified tech co-founders in Morocco",
  "proposedSolution": "Create a dedicated platform for co-founder matching with verified profiles",
  "tamSize": 500000000,
  "tamCurrency": "MAD",
  "samSize": 50000000,
  "samCurrency": "MAD",
  "somSize": 5000000,
  "somCurrency": "MAD",
  "confidenceScore": 75.0,
  "marketPotentialScore": 80.0,
  "feasibilityScore": 70.0,
  "priority": "HIGH",
  "tags": ["fintech", "platform", "networking"],
  "category": "Marketplace",
  "targetAudience": "Entrepreneurs and tech professionals in Morocco"
}
```

**Response (201):**
```json
{
  "id": 2,
  "title": "Fintech Co-founder Matching Platform",
  "description": "A platform to connect entrepreneurs...",
  "status": "DRAFT",
  "createdAt": "2024-01-15T10:00:00Z",
  ...
}
```

---

### PUT /opportunities/{id}
Update an opportunity.

**Authorization:** ANALYST, ADMIN

**Request:**
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "status": "VALIDATED"
}
```

**Response (200):** Updated opportunity object

---

### POST /opportunities/{id}/validate
Validate an opportunity.

**Authorization:** ANALYST, ADMIN

**Request:**
```json
{
  "notes": "Strong market need identified through research"
}
```

**Response (200):** Updated opportunity with status "VALIDATED"

---

### GET /opportunities/high-scoring
Get high-scoring opportunities.

**Query Parameters:**
- `limit` (integer, optional): Number of results (default: 10)

**Response (200):**
```json
[
  {
    "id": 1,
    "title": "Fintech Co-founder Matching Platform",
    "overallScore": 85.5,
    ...
  }
]
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `UNAUTHORIZED` | 401 | Invalid or missing authentication |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `BAD_REQUEST` | 400 | Invalid request parameters |
| `INTERNAL_ERROR` | 500 | Internal server error |
| `VALIDATION_ERROR` | 422 | Request validation failed |

## Rate Limiting

API requests are rate-limited to:
- 100 requests per minute for authenticated users
- 20 requests per minute for unauthenticated users

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642238400
```

## Pagination

List endpoints support pagination with the following parameters:
- `page`: Page number (0-indexed)
- `size`: Number of items per page

Response includes pagination metadata:
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

## Filtering and Sorting

### Sorting
Use `sortBy` and `direction` parameters:
```
GET /posts?sortBy=collectedAt&direction=desc
```

### Filtering
Filter by status or source:
```
GET /posts/status/RAW
GET /opportunities/status/VALIDATED
```
