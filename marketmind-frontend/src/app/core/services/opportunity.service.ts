import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  Opportunity, 
  OpportunityStatus, 
  OpportunityPriority,
  CreateOpportunityRequest,
  UpdateOpportunityRequest 
} from '../../models/opportunity.model';
import { PageResponse } from '../../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class OpportunityService {
  private readonly apiUrl = `${environment.apiUrl}/opportunities`;

  constructor(private http: HttpClient) {}

  getAllOpportunities(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    direction: string = 'desc'
  ): Observable<PageResponse<Opportunity>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    
    return this.http.get<PageResponse<Opportunity>>(this.apiUrl, { params });
  }

  getOpportunityById(id: number): Observable<Opportunity> {
    return this.http.get<Opportunity>(`${this.apiUrl}/${id}`);
  }

  getOpportunitiesByStatus(status: OpportunityStatus): Observable<Opportunity[]> {
    return this.http.get<Opportunity[]>(`${this.apiUrl}/status/${status}`);
  }

  getOpportunitiesByPriority(priority: OpportunityPriority): Observable<Opportunity[]> {
    return this.http.get<Opportunity[]>(`${this.apiUrl}/priority/${priority}`);
  }

  getHighScoringOpportunities(limit: number = 10): Observable<Opportunity[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Opportunity[]>(`${this.apiUrl}/high-scoring`, { params });
  }

  getOpportunitiesByCluster(clusterId: number): Observable<Opportunity[]> {
    return this.http.get<Opportunity[]>(`${this.apiUrl}/cluster/${clusterId}`);
  }

  createOpportunity(request: CreateOpportunityRequest): Observable<Opportunity> {
    return this.http.post<Opportunity>(this.apiUrl, request);
  }

  updateOpportunity(id: number, request: UpdateOpportunityRequest): Observable<Opportunity> {
    return this.http.put<Opportunity>(`${this.apiUrl}/${id}`, request);
  }

  validateOpportunity(id: number, notes: string): Observable<Opportunity> {
    return this.http.post<Opportunity>(`${this.apiUrl}/${id}/validate`, { notes });
  }

  deleteOpportunity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getOpportunitiesByStatusStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/stats/by-status`);
  }

  getOpportunitiesByPriorityStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/stats/by-priority`);
  }

  getAverageScores(): Observable<{
    overall: number;
    confidence: number;
    marketPotential: number;
    feasibility: number;
  }> {
    return this.http.get<any>(`${this.apiUrl}/stats/average-scores`);
  }
}
