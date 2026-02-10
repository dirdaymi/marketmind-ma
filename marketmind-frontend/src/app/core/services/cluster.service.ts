import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cluster } from '../../models/cluster.model';
import { PageResponse } from '../../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class ClusterService {
  private readonly apiUrl = `${environment.apiUrl}/clusters`;

  constructor(private http: HttpClient) {}

  getAllClusters(
    page: number = 0,
    size: number = 20,
    activeOnly: boolean = true
  ): Observable<PageResponse<Cluster>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('activeOnly', activeOnly.toString());
    
    return this.http.get<PageResponse<Cluster>>(this.apiUrl, { params });
  }

  getClusterById(id: number): Observable<Cluster> {
    return this.http.get<Cluster>(`${this.apiUrl}/${id}`);
  }

  getClusterByLabel(label: string): Observable<Cluster> {
    return this.http.get<Cluster>(`${this.apiUrl}/label/${label}`);
  }

  getTopClusters(limit: number = 10): Observable<Cluster[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Cluster[]>(`${this.apiUrl}/top`, { params });
  }

  getClustersByTerm(term: string): Observable<Cluster[]> {
    const params = new HttpParams().set('term', term);
    return this.http.get<Cluster[]>(`${this.apiUrl}/search`, { params });
  }

  createCluster(cluster: Partial<Cluster>): Observable<Cluster> {
    return this.http.post<Cluster>(this.apiUrl, cluster);
  }

  updateCluster(id: number, cluster: Partial<Cluster>): Observable<Cluster> {
    return this.http.put<Cluster>(`${this.apiUrl}/${id}`, cluster);
  }

  deleteCluster(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  runClustering(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/run-clustering`, {});
  }

  countActiveClusters(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/count`);
  }

  getAveragePostsPerCluster(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/avg-posts`);
  }
}
