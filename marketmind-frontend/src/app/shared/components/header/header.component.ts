import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';

import { User } from '../../../models/user.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    MatToolbarModule, 
    MatButtonModule, 
    MatIconModule,
    MatMenuModule,
    MatBadgeModule
  ],
  template: `
    <mat-toolbar color="primary" class="header-toolbar">
      <button mat-icon-button (click)="onToggleSidebar()">
        <mat-icon>menu</mat-icon>
      </button>
      
      <span class="spacer"></span>
      
      <div class="user-section" *ngIf="isLoggedIn && user">
        <button mat-button [matMenuTriggerFor]="userMenu">
          <mat-icon>account_circle</mat-icon>
          <span class="username">{{ user.username }}</span>
          <mat-icon>expand_more</mat-icon>
        </button>
        
        <mat-menu #userMenu="matMenu">
          <div class="user-info" mat-menu-item disabled>
            <span>{{ user.email }}</span>
            <small>{{ user.roles?.join(', ') }}</small>
          </div>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="onLogout()">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
          </button>
        </mat-menu>
      </div>
      
      <div class="guest-section" *ngIf="!isLoggedIn">
        <a mat-button routerLink="/login">
          <mat-icon>login</mat-icon>
          Login
        </a>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .header-toolbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    .spacer {
      flex: 1 1 auto;
    }
    
    .user-section {
      display: flex;
      align-items: center;
      
      button {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .username {
        margin: 0 4px;
      }
    }
    
    .user-info {
      display: flex;
      flex-direction: column;
      padding: 8px 16px;
      
      span {
        font-weight: 500;
      }
      
      small {
        opacity: 0.7;
      }
    }
    
    .guest-section {
      a {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  `]
})
export class HeaderComponent {
  @Input() isLoggedIn = false;
  @Input() user: User | null = null;
  
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  onLogout(): void {
    this.logout.emit();
  }
}
