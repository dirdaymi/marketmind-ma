import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RawPost, PageResponse, PostSource, PostStatus } from '../../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private readonly apiUrl = `${environment.apiUrl}/posts`;

  constructor(private http: HttpClient) {}

  getAllPosts(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'collectedAt',
    direction: string = 'desc'
  ): Observable<PageResponse<RawPost>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    
    return this.http.get<PageResponse<RawPost>>(this.apiUrl, { params });
  }

  getPostById(id: number): Observable<RawPost> {
    return this.http.get<RawPost>(`${this.apiUrl}/${id}`);
  }

  getPostByExternalId(externalId: string): Observable<RawPost> {
    return this.http.get<RawPost>(`${this.apiUrl}/external/${externalId}`);
  }

  getPostsBySource(
    source: PostSource,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<RawPost>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<RawPost>>(
      `${this.apiUrl}/source/${source}`,
      { params }
    );
  }

  getPostsByStatus(
    status: PostStatus,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<RawPost>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<RawPost>>(
      `${this.apiUrl}/status/${status}`,
      { params }
    );
  }

  getPostsWithoutEmbeddings(): Observable<RawPost[]> {
    return this.http.get<RawPost[]>(`${this.apiUrl}/without-embeddings`);
  }

  searchPosts(query: string): Observable<RawPost[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<RawPost[]>(`${this.apiUrl}/search`, { params });
  }

  getPostsBySourceStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/stats/by-source`);
  }

  getPostsByStatusStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/stats/by-status`);
  }

  updatePostStatus(id: number, status: PostStatus): Observable<void> {
    const params = new HttpParams().set('status', status);
    return this.http.put<void>(`${this.apiUrl}/${id}/status`, {}, { params });
  }
}
