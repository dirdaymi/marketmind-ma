-- MarketMind MA - Database Initialization Script
-- PostgreSQL 15+ with pgvector extension
-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Create schema
CREATE SCHEMA IF NOT EXISTS marketmind;

-- Set search path
SET search_path TO marketmind, public;

-- ============================================
-- ENUM TYPES
-- ============================================
CREATE TYPE post_source AS ENUM (
    'REDDIT',
    'BLADI_NET',
    'DEVELOPEZ_COM',
    'HACKER_NEWS',
    'MEDIUM',
    'RSS_FEED',
    'TWITTER'
);

CREATE TYPE post_status AS ENUM (
    'RAW',
    'PROCESSED',
    'CLUSTERED',
    'ANALYZED',
    'ARCHIVED'
);

CREATE TYPE opportunity_status AS ENUM (
    'DRAFT',
    'VALIDATED',
    'REJECTED',
    'IN_PROGRESS',
    'IMPLEMENTED'
);

CREATE TYPE opportunity_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

-- ============================================
-- RAW POSTS TABLE
-- ============================================
CREATE TABLE raw_posts (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    source post_source NOT NULL,
    source_url TEXT,
    title TEXT,
    content TEXT NOT NULL,
    author VARCHAR(255),
    language VARCHAR(10) DEFAULT 'unknown',
    posted_at TIMESTAMP WITH TIME ZONE,
    collected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status post_status DEFAULT 'RAW',
    keywords TEXT[],
    metadata JSONB DEFAULT '{}',
    raw_data JSONB DEFAULT '{}',
    
    -- Full-text search
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', COALESCE(title, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(content, '')), 'B')
    ) STORED,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for raw_posts
CREATE INDEX idx_raw_posts_external_id ON raw_posts(external_id);
CREATE INDEX idx_raw_posts_source ON raw_posts(source);
CREATE INDEX idx_raw_posts_status ON raw_posts(status);
CREATE INDEX idx_raw_posts_language ON raw_posts(language);
CREATE INDEX idx_raw_posts_posted_at ON raw_posts(posted_at);
CREATE INDEX idx_raw_posts_keywords ON raw_posts USING GIN(keywords);
CREATE INDEX idx_raw_posts_search ON raw_posts USING GIN(search_vector);
CREATE INDEX idx_raw_posts_metadata ON raw_posts USING GIN(metadata);

-- ============================================
-- EMBEDDINGS TABLE (pgvector)
-- ============================================
CREATE TABLE embeddings (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES raw_posts(id) ON DELETE CASCADE,
    embedding vector(384) NOT NULL,  -- 384-dim for all-MiniLM-L6-v2
    model_name VARCHAR(100) DEFAULT 'sentence-transformers/all-MiniLM-L6-v2',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_post_embedding UNIQUE (post_id)
);

-- Vector similarity index (IVFFlat for approximate nearest neighbor search)
CREATE INDEX idx_embeddings_vector ON embeddings 
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- Index for finding embeddings by post
CREATE INDEX idx_embeddings_post_id ON embeddings(post_id);

-- ============================================
-- CLUSTERS TABLE
-- ============================================
CREATE TABLE clusters (
    id BIGSERIAL PRIMARY KEY,
    cluster_label VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Cluster statistics
    post_count INTEGER DEFAULT 0,
    avg_confidence FLOAT DEFAULT 0.0,
    
    -- Cluster centroid (for similarity calculations)
    centroid vector(384),
    
    -- Key terms extracted from cluster
    key_terms TEXT[],
    
    -- Representative posts (JSON array of post IDs)
    representative_posts JSONB DEFAULT '[]',
    
    -- Cluster metadata
    dbscan_params JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Soft delete
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_clusters_label ON clusters(cluster_label);
CREATE INDEX idx_clusters_active ON clusters(is_active);
CREATE INDEX idx_clusters_post_count ON clusters(post_count);

-- ============================================
-- CLUSTER MEMBERSHIP TABLE (Many-to-Many)
-- ============================================
CREATE TABLE cluster_memberships (
    id BIGSERIAL PRIMARY KEY,
    cluster_id BIGINT NOT NULL REFERENCES clusters(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES raw_posts(id) ON DELETE CASCADE,
    distance_to_centroid FLOAT,
    is_core_point BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_cluster_post UNIQUE (cluster_id, post_id)
);

CREATE INDEX idx_cluster_memberships_cluster ON cluster_memberships(cluster_id);
CREATE INDEX idx_cluster_memberships_post ON cluster_memberships(post_id);

-- ============================================
-- OPPORTUNITIES TABLE
-- ============================================
CREATE TABLE opportunities (
    id BIGSERIAL PRIMARY KEY,
    cluster_id BIGINT REFERENCES clusters(id) ON DELETE SET NULL,
    
    -- Opportunity identification
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    problem_statement TEXT,
    proposed_solution TEXT,
    
    -- Market sizing (TAM/SAM/SOM)
    tam_size BIGINT,  -- Total Addressable Market
    tam_currency VARCHAR(3) DEFAULT 'MAD',
    sam_size BIGINT,  -- Serviceable Addressable Market
    sam_currency VARCHAR(3) DEFAULT 'MAD',
    som_size BIGINT,  -- Serviceable Obtainable Market
    som_currency VARCHAR(3) DEFAULT 'MAD',
    
    -- Market sizing assumptions
    market_assumptions JSONB DEFAULT '{}',
    
    -- Competition analysis
    competitors JSONB DEFAULT '[]',
    competitive_advantage TEXT,
    
    -- Scoring
    confidence_score FLOAT DEFAULT 0.0,  -- 0-100
    market_potential_score FLOAT DEFAULT 0.0,  -- 0-100
    feasibility_score FLOAT DEFAULT 0.0,  -- 0-100
    overall_score FLOAT GENERATED ALWAYS AS (
        (confidence_score + market_potential_score + feasibility_score) / 3.0
    ) STORED,
    
    -- Status and workflow
    status opportunity_status DEFAULT 'DRAFT',
    priority opportunity_priority DEFAULT 'MEDIUM',
    
    -- Validation
    validated_by VARCHAR(255),
    validated_at TIMESTAMP WITH TIME ZONE,
    validation_notes TEXT,
    
    -- Enrichment
    tags TEXT[],
    category VARCHAR(100),
    target_audience TEXT,
    
    -- Traceability
    source_posts JSONB DEFAULT '[]',
    analysis_metadata JSONB DEFAULT '{}',
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'system'
);

CREATE INDEX idx_opportunities_cluster ON opportunities(cluster_id);
CREATE INDEX idx_opportunities_status ON opportunities(status);
CREATE INDEX idx_opportunities_priority ON opportunities(priority);
CREATE INDEX idx_opportunities_score ON opportunities(overall_score);
CREATE INDEX idx_opportunities_category ON opportunities(category);
CREATE INDEX idx_opportunities_tags ON opportunities USING GIN(tags);
CREATE INDEX idx_opportunities_created_at ON opportunities(created_at);

-- ============================================
-- USERS TABLE (for JWT authentication)
-- ============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    
    -- Roles: ADMIN, ANALYST, VIEWER
    roles TEXT[] DEFAULT '{VIEWER}',
    
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- ============================================
-- AUDIT LOG TABLE
-- ============================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- ============================================
-- DATA COLLECTION JOBS TABLE
-- ============================================
CREATE TABLE collection_jobs (
    id BIGSERIAL PRIMARY KEY,
    source post_source NOT NULL,
    job_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    
    -- Job parameters
    parameters JSONB DEFAULT '{}',
    
    -- Results
    posts_collected INTEGER DEFAULT 0,
    posts_new INTEGER DEFAULT 0,
    posts_duplicated INTEGER DEFAULT 0,
    errors JSONB DEFAULT '[]',
    
    -- Timing
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_collection_jobs_source ON collection_jobs(source);
CREATE INDEX idx_collection_jobs_status ON collection_jobs(status);
CREATE INDEX idx_collection_jobs_created ON collection_jobs(created_at);

-- ============================================
-- TRIGGER FUNCTIONS FOR UPDATED_AT
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at trigger to all tables
CREATE TRIGGER update_raw_posts_updated_at 
    BEFORE UPDATE ON raw_posts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clusters_updated_at 
    BEFORE UPDATE ON clusters 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_opportunities_updated_at 
    BEFORE UPDATE ON opportunities 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- VIEWS FOR ANALYTICS
-- ============================================

-- Daily collection stats
CREATE VIEW v_daily_collection_stats AS
SELECT 
    DATE(collected_at) as collection_date,
    source,
    COUNT(*) as post_count,
    COUNT(DISTINCT language) as language_count
FROM raw_posts
GROUP BY DATE(collected_at), source
ORDER BY collection_date DESC, source;

-- Cluster summary view
CREATE VIEW v_cluster_summary AS
SELECT 
    c.id,
    c.cluster_label,
    c.name,
    c.post_count,
    c.avg_confidence,
    c.key_terms,
    c.is_active,
    COUNT(o.id) as opportunity_count,
    MAX(o.overall_score) as max_opportunity_score
FROM clusters c
LEFT JOIN opportunities o ON o.cluster_id = c.id
GROUP BY c.id, c.cluster_label, c.name, c.post_count, c.avg_confidence, c.key_terms, c.is_active;

-- Opportunity pipeline view
CREATE VIEW v_opportunity_pipeline AS
SELECT 
    status,
    priority,
    COUNT(*) as count,
    AVG(overall_score) as avg_score,
    MIN(created_at) as oldest_created,
    MAX(created_at) as newest_created
FROM opportunities
GROUP BY status, priority;

-- ============================================
-- INITIAL DATA
-- ============================================

-- Insert default admin user (password: admin123 - change in production!)
-- Password hash is BCrypt encoded "admin123"
INSERT INTO users (username, email, password_hash, first_name, last_name, roles)
VALUES (
    'admin',
    'admin@marketmind.ma',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- admin123
    'System',
    'Administrator',
    '{ADMIN,ANALYST,VIEWER}'
);

-- Insert sample analyst user (password: analyst123)
INSERT INTO users (username, email, password_hash, first_name, last_name, roles)
VALUES (
    'analyst',
    'analyst@marketmind.ma',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- analyst123
    'Market',
    'Analyst',
    '{ANALYST,VIEWER}'
);

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================
COMMENT ON TABLE raw_posts IS 'Stores raw collected posts from various sources';
COMMENT ON TABLE embeddings IS 'Vector embeddings for semantic similarity search using pgvector';
COMMENT ON TABLE clusters IS 'DBSCAN clusters of similar posts representing potential opportunities';
COMMENT ON TABLE opportunities IS 'Validated business opportunities derived from clusters';
COMMENT ON TABLE users IS 'Application users for JWT authentication';
COMMENT ON TABLE audit_logs IS 'Audit trail for all significant operations';
COMMENT ON TABLE collection_jobs IS 'Tracking for data collection jobs';
