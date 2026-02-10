import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatIconModule],
  template: `
    <div class="sidebar-container">
      <div class="logo-section" *ngIf="!isCollapsed">
        <h2>MarketMind MA</h2>
        <p>Market Intelligence</p>
      </div>
      
      <mat-nav-list>
        <a mat-list-item 
           *ngFor="let item of filteredNavItems"
           [routerLink]="item.route"
           routerLinkActive="active-link"
           [matTooltip]="isCollapsed ? item.label : ''"
           matTooltipPosition="right">
          <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
          <span matListItemTitle *ngIf="!isCollapsed">{{ item.label }}</span>
        </a>
      </mat-nav-list>
    </div>
  `,
  styles: [`
    .sidebar-container {
      height: 100%;
      background: #1a237e;
      color: white;
    }
    
    .logo-section {
      padding: 24px 16px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      h2 {
        margin: 0;
        font-size: 1.25rem;
        font-weight: 500;
      }
      
      p {
        margin: 4px 0 0;
        font-size: 0.75rem;
        opacity: 0.7;
      }
    }
    
    mat-nav-list {
      padding-top: 8px;
    }
    
    a[mat-list-item] {
      color: rgba(255, 255, 255, 0.85);
      margin: 4px 8px;
      border-radius: 4px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: white;
      }
      
      &.active-link {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
      
      mat-icon {
        color: inherit;
      }
    }
  `]
})
export class SidebarComponent {
  @Input() isCollapsed = false;
  @Input() userRole: string | null = null;

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { label: 'Posts', icon: 'article', route: '/posts', roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { label: 'Clusters', icon: 'group_work', route: '/clusters', roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { label: 'Opportunities', icon: 'lightbulb', route: '/opportunities', roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { label: 'Analytics', icon: 'analytics', route: '/analytics', roles: ['ANALYST', 'ADMIN'] },
    { label: 'Admin', icon: 'admin_panel_settings', route: '/admin', roles: ['ADMIN'] }
  ];

  get filteredNavItems(): NavItem[] {
    return this.navItems.filter(item => 
      item.roles.includes(this.userRole || 'VIEWER')
    );
  }
}
