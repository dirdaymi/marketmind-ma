import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule, MatCardModule],
  template: `
    <div class="not-found-container">
      <mat-card class="not-found-card">
        <mat-card-content>
          <div class="icon-container">
            <mat-icon>search_off</mat-icon>
          </div>
          <h1>404</h1>
          <h2>Page Not Found</h2>
          <p>The page you are looking for does not exist or has been moved.</p>
          <button mat-raised-button color="primary" routerLink="/dashboard">
            <mat-icon>home</mat-icon>
            Go to Dashboard
          </button>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 24px;
    }
    
    .not-found-card {
      max-width: 500px;
      text-align: center;
      padding: 48px 32px;
    }
    
    .icon-container {
      margin-bottom: 24px;
      
      mat-icon {
        font-size: 96px;
        width: 96px;
        height: 96px;
        color: #9e9e9e;
      }
    }
    
    h1 {
      font-size: 96px;
      font-weight: 300;
      margin: 0;
      color: #3f51b5;
      line-height: 1;
    }
    
    h2 {
      font-size: 24px;
      font-weight: 500;
      margin: 16px 0;
      color: #333;
    }
    
    p {
      font-size: 16px;
      color: #666;
      margin-bottom: 32px;
    }
    
    button {
      mat-icon {
        margin-right: 8px;
      }
    }
  `]
})
export class NotFoundComponent {}
