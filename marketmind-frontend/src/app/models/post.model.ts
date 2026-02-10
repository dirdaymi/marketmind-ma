export enum PostSource {
  REDDIT = 'REDDIT',
  BLADI_NET = 'BLADI_NET',
  DEVELOPEZ_COM = 'DEVELOPEZ_COM',
  HACKER_NEWS = 'HACKER_NEWS',
  MEDIUM = 'MEDIUM',
  RSS_FEED = 'RSS_FEED',
  TWITTER = 'TWITTER'
}

export enum PostStatus {
  RAW = 'RAW',
  PROCESSED = 'PROCESSED',
  CLUSTERED = 'CLUSTERED',
  ANALYZED = 'ANALYZED',
  ARCHIVED = 'ARCHIVED'
}

export interface RawPost {
  id: number;
  externalId: string;
  source: PostSource;
  sourceUrl?: string;
  title?: string;
  content: string;
  author?: string;
  language?: string;
  postedAt?: string;
  collectedAt: string;
  status: PostStatus;
  keywords?: string[];
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
