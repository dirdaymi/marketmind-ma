"""
MarketMind MA - NLP Microservice
FastAPI application for text embeddings and clustering
"""

import logging
import os
from contextlib import asynccontextmanager
from typing import List, Optional

import numpy as np
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sklearn.cluster import DBSCAN

from services.embedding_service import EmbeddingService
from services.text_processing import TextProcessor

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# Global services
embedding_service: Optional[EmbeddingService] = None
text_processor: Optional[TextProcessor] = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan manager"""
    global embedding_service, text_processor
    
    logger.info("Starting MarketMind NLP Service...")
    
    # Initialize services
    model_name = os.getenv("MODEL_NAME", "sentence-transformers/all-MiniLM-L6-v2")
    logger.info(f"Loading embedding model: {model_name}")
    
    embedding_service = EmbeddingService(model_name=model_name)
    text_processor = TextProcessor()
    
    logger.info("NLP Service initialized successfully")
    
    yield
    
    # Cleanup
    logger.info("Shutting down MarketMind NLP Service...")


# Create FastAPI application
app = FastAPI(
    title="MarketMind MA - NLP Service",
    description="NLP microservice for text embeddings and clustering",
    version="1.0.0",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============== Pydantic Models ==============

class TextInput(BaseModel):
    post_id: int = Field(..., description="Post ID")
    text: str = Field(..., description="Text to embed")


class EmbeddingRequest(BaseModel):
    texts: List[TextInput] = Field(..., description="List of texts to embed")


class EmbeddingResult(BaseModel):
    post_id: int
    embedding: List[float]
    success: bool
    error: Optional[str] = None


class EmbeddingResponse(BaseModel):
    embeddings: List[EmbeddingResult]
    model: str
    dimension: int


class EmbeddingData(BaseModel):
    post_id: int
    embedding: List[float]


class ClusteringRequest(BaseModel):
    embeddings: List[EmbeddingData]
    eps: float = Field(default=0.5, description="DBSCAN epsilon parameter")
    min_samples: int = Field(default=5, description="DBSCAN min samples parameter")


class ClusterResult(BaseModel):
    label: str
    post_ids: List[int]
    centroid: List[float]
    key_terms: List[str]
    size: int
    avg_confidence: float


class ClusteringResponse(BaseModel):
    clusters: List[ClusterResult]
    total_posts: int
    clustered_posts: int
    noise_points: int
    parameters: dict


class TextAnalysisRequest(BaseModel):
    text: str = Field(..., description="Text to analyze")


class TextAnalysisResponse(BaseModel):
    language: str
    cleaned_text: str
    tokens: List[str]
    entities: List[dict]
    sentiment: Optional[str] = None


class HealthResponse(BaseModel):
    status: str
    model: str
    version: str


# ============== API Endpoints ==============

@app.get("/", response_model=HealthResponse)
async def root():
    """Root endpoint - health check"""
    return HealthResponse(
        status="healthy",
        model=embedding_service.model_name if embedding_service else "unknown",
        version="1.0.0"
    )


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy",
        model=embedding_service.model_name if embedding_service else "unknown",
        version="1.0.0"
    )


@app.post("/api/v1/nlp/embed", response_model=EmbeddingResponse)
async def generate_embeddings(request: EmbeddingRequest):
    """
    Generate embeddings for a list of texts
    """
    try:
        logger.info(f"Generating embeddings for {len(request.texts)} texts")
        
        results = []
        for item in request.texts:
            try:
                # Clean text before embedding
                cleaned_text = text_processor.clean_text(item.text)
                
                # Generate embedding
                embedding = embedding_service.encode(cleaned_text)
                
                results.append(EmbeddingResult(
                    post_id=item.post_id,
                    embedding=embedding.tolist(),
                    success=True
                ))
            except Exception as e:
                logger.error(f"Error embedding post {item.post_id}: {e}")
                results.append(EmbeddingResult(
                    post_id=item.post_id,
                    embedding=[],
                    success=False,
                    error=str(e)
                ))
        
        return EmbeddingResponse(
            embeddings=results,
            model=embedding_service.model_name,
            dimension=embedding_service.dimension
        )
        
    except Exception as e:
        logger.error(f"Error in embed endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Embedding generation failed: {str(e)}"
        )


@app.post("/api/v1/nlp/embed/single")
async def generate_single_embedding(request: TextAnalysisRequest):
    """
    Generate embedding for a single text
    """
    try:
        cleaned_text = text_processor.clean_text(request.text)
        embedding = embedding_service.encode(cleaned_text)
        
        return {
            "embedding": embedding.tolist(),
            "model": embedding_service.model_name,
            "dimension": embedding_service.dimension,
            "cleaned_text": cleaned_text
        }
        
    except Exception as e:
        logger.error(f"Error in single embed endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Embedding generation failed: {str(e)}"
        )


@app.post("/api/v1/nlp/cluster", response_model=ClusteringResponse)
async def cluster_embeddings(request: ClusteringRequest):
    """
    Perform DBSCAN clustering on embeddings
    """
    try:
        logger.info(f"Clustering {len(request.embeddings)} embeddings")
        
        if len(request.embeddings) < request.min_samples:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Not enough embeddings for clustering. Need at least {request.min_samples}"
            )
        
        # Prepare data for clustering
        post_ids = []
        embedding_matrix = []
        
        for item in request.embeddings:
            post_ids.append(item.post_id)
            embedding_matrix.append(item.embedding)
        
        X = np.array(embedding_matrix)
        
        # Perform DBSCAN clustering
        dbscan = DBSCAN(
            eps=request.eps,
            min_samples=request.min_samples,
            metric='cosine'
        )
        
        labels = dbscan.fit_predict(X)
        
        # Build clusters
        clusters = {}
        noise_count = 0
        
        for idx, label in enumerate(labels):
            if label == -1:
                noise_count += 1
                continue
            
            label_str = f"CLUSTER_{label + 1}"
            if label_str not in clusters:
                clusters[label_str] = {
                    "post_ids": [],
                    "embeddings": []
                }
            
            clusters[label_str]["post_ids"].append(post_ids[idx])
            clusters[label_str]["embeddings"].append(embedding_matrix[idx])
        
        # Build response
        cluster_results = []
        for label, data in clusters.items():
            embeddings_array = np.array(data["embeddings"])
            centroid = np.mean(embeddings_array, axis=0).tolist()
            
            # Extract key terms from cluster (simplified)
            key_terms = extract_key_terms_from_cluster(data["post_ids"])
            
            cluster_results.append(ClusterResult(
                label=label,
                post_ids=data["post_ids"],
                centroid=centroid,
                key_terms=key_terms,
                size=len(data["post_ids"]),
                avg_confidence=0.85  # Placeholder
            ))
        
        clustered_posts = sum(c.size for c in cluster_results)
        
        return ClusteringResponse(
            clusters=cluster_results,
            total_posts=len(request.embeddings),
            clustered_posts=clustered_posts,
            noise_points=noise_count,
            parameters={
                "eps": request.eps,
                "min_samples": request.min_samples,
                "metric": "cosine",
                "algorithm": "DBSCAN"
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in cluster endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Clustering failed: {str(e)}"
        )


@app.post("/api/v1/nlp/analyze", response_model=TextAnalysisResponse)
async def analyze_text(request: TextAnalysisRequest):
    """
    Analyze text - detect language, clean, tokenize, extract entities
    """
    try:
        # Detect language
        language = text_processor.detect_language(request.text)
        
        # Clean text
        cleaned_text = text_processor.clean_text(request.text)
        
        # Tokenize
        tokens = text_processor.tokenize(cleaned_text, language)
        
        # Extract entities (simplified)
        entities = text_processor.extract_entities(cleaned_text)
        
        return TextAnalysisResponse(
            language=language,
            cleaned_text=cleaned_text,
            tokens=tokens[:50],  # Limit tokens in response
            entities=entities
        )
        
    except Exception as e:
        logger.error(f"Error in analyze endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Text analysis failed: {str(e)}"
        )


@app.post("/api/v1/nlp/similarity")
async def calculate_similarity(request: dict):
    """
    Calculate cosine similarity between two texts
    """
    try:
        text1 = request.get("text1", "")
        text2 = request.get("text2", "")
        
        if not text1 or not text2:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Both text1 and text2 are required"
            )
        
        # Clean and embed texts
        cleaned1 = text_processor.clean_text(text1)
        cleaned2 = text_processor.clean_text(text2)
        
        embedding1 = embedding_service.encode(cleaned1)
        embedding2 = embedding_service.encode(cleaned2)
        
        # Calculate cosine similarity
        similarity = np.dot(embedding1, embedding2) / (
            np.linalg.norm(embedding1) * np.linalg.norm(embedding2)
        )
        
        return {
            "similarity": float(similarity),
            "text1_cleaned": cleaned1,
            "text2_cleaned": cleaned2
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in similarity endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Similarity calculation failed: {str(e)}"
        )


# ============== Helper Functions ==============

def extract_key_terms_from_cluster(post_ids: List[int]) -> List[str]:
    """
    Extract key terms from a cluster (simplified implementation)
    In production, this would analyze the actual post content
    """
    # Placeholder - would use TF-IDF or similar in production
    return ["startup", "tech", "morocco", "business"]


# ============== Main Entry Point ==============

if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    reload = os.getenv("RELOAD", "false").lower() == "true"
    
    uvicorn.run(
        "app:app",
        host=host,
        port=port,
        reload=reload,
        log_level="info"
    )
