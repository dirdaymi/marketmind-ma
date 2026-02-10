import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

import { LoginRequest, AuthResponse, User } from '../../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'marketmind_token';
  private readonly userKey = 'marketmind_user';
  
  // Signals for reactive state
  private _isLoggedIn = signal<boolean>(false);
  private _currentUser = signal<User | null>(null);
  
  // Computed signals
  public isLoggedIn = computed(() => this._isLoggedIn());
  public currentUser = computed(() => this._currentUser());
  public userRole = computed(() => {
    const user = this._currentUser();
    return user?.roles?.[0] || 'VIEWER';
  });

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.checkAuthStatus();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(error => {
          this.showError('Login failed. Please check your credentials.');
          return throwError(() => error);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this._isLoggedIn.set(false);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
    this.showSuccess('Logged out successfully');
  }

  refreshToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {})
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(error => {
          this.logout();
          return throwError(() => error);
        })
      );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  checkAuthStatus(): void {
    const token = this.getToken();
    const userStr = localStorage.getItem(this.userKey);
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        this._isLoggedIn.set(true);
        this._currentUser.set(user);
      } catch (e) {
        this.logout();
      }
    }
  }

  hasRole(roles: string[]): boolean {
    const user = this._currentUser();
    if (!user) return false;
    return user.roles.some(role => roles.includes(role));
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    
    const user: User = {
      id: response.userId,
      username: response.username,
      email: response.email,
      roles: response.roles,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this._isLoggedIn.set(true);
    this._currentUser.set(user);
    
    this.showSuccess('Login successful');
    this.router.navigate(['/dashboard']);
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}
