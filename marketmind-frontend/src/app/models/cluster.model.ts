export interface Cluster {
  id: number;
  clusterLabel: string;
  name: string;
  description?: string;
  postCount: number;
  avgConfidence: number;
  keyTerms?: string[];
  representativePosts?: RepresentativePost[];
  dbscanParams?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  opportunityCount?: number;
  maxOpportunityScore?: number;
}

export interface RepresentativePost {
  id: number;
  title?: string;
  content: string;
  source: string;
}

export interface ClusterMembership {
  id: number;
  clusterId: number;
  postId: number;
  distanceToCentroid?: number;
  isCorePoint: boolean;
}
