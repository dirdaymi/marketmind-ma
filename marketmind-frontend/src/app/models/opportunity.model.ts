export enum OpportunityStatus {
  DRAFT = 'DRAFT',
  VALIDATED = 'VALIDATED',
  REJECTED = 'REJECTED',
  IN_PROGRESS = 'IN_PROGRESS',
  IMPLEMENTED = 'IMPLEMENTED'
}

export enum OpportunityPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface Opportunity {
  id: number;
  clusterId?: number;
  clusterName?: string;
  
  title: string;
  description: string;
  problemStatement?: string;
  proposedSolution?: string;
  
  // Market sizing
  tamSize?: number;
  tamCurrency: string;
  samSize?: number;
  samCurrency: string;
  somSize?: number;
  somCurrency: string;
  marketAssumptions?: Record<string, any>;
  
  // Competition
  competitors?: Competitor[];
  competitiveAdvantage?: string;
  
  // Scoring
  confidenceScore: number;
  marketPotentialScore: number;
  feasibilityScore: number;
  overallScore?: number;
  
  // Status
  status: OpportunityStatus;
  priority: OpportunityPriority;
  
  // Validation
  validatedBy?: string;
  validatedAt?: string;
  validationNotes?: string;
  
  // Enrichment
  tags?: string[];
  category?: string;
  targetAudience?: string;
  
  // Traceability
  sourcePosts?: SourcePost[];
  analysisMetadata?: Record<string, any>;
  
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface Competitor {
  name: string;
  description?: string;
  website?: string;
  strength?: string;
}

export interface SourcePost {
  id: number;
  title?: string;
  content: string;
  source: string;
}

export interface CreateOpportunityRequest {
  clusterId?: number;
  title: string;
  description: string;
  problemStatement?: string;
  proposedSolution?: string;
  tamSize?: number;
  tamCurrency?: string;
  samSize?: number;
  samCurrency?: string;
  somSize?: number;
  somCurrency?: string;
  marketAssumptions?: Record<string, any>;
  competitors?: Competitor[];
  competitiveAdvantage?: string;
  confidenceScore?: number;
  marketPotentialScore?: number;
  feasibilityScore?: number;
  priority?: OpportunityPriority;
  tags?: string[];
  category?: string;
  targetAudience?: string;
}

export interface UpdateOpportunityRequest {
  title?: string;
  description?: string;
  problemStatement?: string;
  proposedSolution?: string;
  tamSize?: number;
  tamCurrency?: string;
  samSize?: number;
  samCurrency?: string;
  somSize?: number;
  somCurrency?: string;
  marketAssumptions?: Record<string, any>;
  competitors?: Competitor[];
  competitiveAdvantage?: string;
  confidenceScore?: number;
  marketPotentialScore?: number;
  feasibilityScore?: number;
  status?: OpportunityStatus;
  priority?: OpportunityPriority;
  validationNotes?: string;
  tags?: string[];
  category?: string;
  targetAudience?: string;
}
