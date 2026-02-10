import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';

import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    SidebarComponent,
    HeaderComponent
  ],
  template: `
    <div class="app-container">
      <app-header 
        [isLoggedIn]="authService.isLoggedIn()"
        [user]="authService.currentUser()"
        (toggleSidebar)="toggleSidebar()"
        (logout)="logout()">
      </app-header>
      
      <mat-sidenav-container class="sidenav-container">
        <mat-sidenav 
          #sidenav 
          mode="side" 
          opened="true"
          class="sidenav"
          [class.collapsed]="isCollapsed">
          <app-sidebar 
            [isCollapsed]="isCollapsed"
            [userRole]="authService.userRole()">
          </app-sidebar>
        </mat-sidenav>
        
        <mat-sidenav-content class="content" [class.expanded]="isCollapsed">
          <div class="main-content">
            <router-outlet></router-outlet>
          </div>
        </mat-sidenav-content>
      </mat-sidenav-container>
    </div>
  `,
  styles: [`
    .app-container {
      height: 100vh;
      display: flex;
      flex-direction: column;
    }
    
    .sidenav-container {
      flex: 1;
    }
    
    .sidenav {
      width: 260px;
      background: #1a237e;
      transition: width 0.3s ease;
    }
    
    .sidenav.collapsed {
      width: 64px;
    }
    
    .content {
      background: #f5f5f5;
      transition: margin-left 0.3s ease;
    }
    
    .main-content {
      padding: 24px;
      min-height: calc(100vh - 64px);
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'MarketMind MA';
  isCollapsed = false;

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    // Check if user is already logged in
    this.authService.checkAuthStatus();
  }

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  logout(): void {
    this.authService.logout();
  }
}
