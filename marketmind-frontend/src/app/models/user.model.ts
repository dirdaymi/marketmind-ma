export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  isActive: boolean;
  lastLogin?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  username: string;
  email: string;
  roles: string[];
  expiresIn: number;
}

export interface DashboardStats {
  // Post statistics
  totalPosts: number;
  postsLast24Hours: number;
  postsLast7Days: number;
  postsLast30Days: number;
  postsBySource: Record<string, number>;
  postsByStatus: Record<string, number>;
  
  // Cluster statistics
  totalClusters: number;
  activeClusters: number;
  averagePostsPerCluster: number;
  
  // Opportunity statistics
  totalOpportunities: number;
  opportunitiesByStatusDraft: number;
  opportunitiesByStatusValidated: number;
  opportunitiesByStatusInProgress: number;
  opportunitiesByStatusImplemented: number;
  averageOpportunityScore: number;
  averageConfidenceScore: number;
  averageMarketPotentialScore: number;
  averageFeasibilityScore: number;
  
  // Collection statistics
  totalCollectionJobs: number;
  totalPostsCollected: number;
  totalPostsNew: number;
}
