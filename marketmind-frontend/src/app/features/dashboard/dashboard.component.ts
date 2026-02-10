import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';

import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

import { DashboardService } from '../../core/services/dashboard.service';
import { OpportunityService } from '../../core/services/opportunity.service';
import { DashboardStats } from '../../models/user.model';
import { Opportunity } from '../../models/opportunity.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatChipsModule,
    BaseChartDirective
  ],
  template: `
    <div class="dashboard-container">
      <div class="page-header">
        <h1>Dashboard</h1>
        <p class="subtitle">Market Intelligence Overview</p>
      </div>

      <!-- Stats Cards -->
      <div class="stats-grid" *ngIf="stats">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon posts">
              <mat-icon>article</mat-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalPosts | number }}</h3>
              <p>Total Posts</p>
              <small>+{{ stats.postsLast24Hours }} in 24h</small>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon clusters">
              <mat-icon>group_work</mat-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalClusters | number }}</h3>
              <p>Clusters</p>
              <small>{{ stats.activeClusters }} active</small>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon opportunities">
              <mat-icon>lightbulb</mat-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalOpportunities | number }}</h3>
              <p>Opportunities</p>
              <small>{{ stats.opportunitiesByStatusValidated }} validated</small>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon score">
              <mat-icon>trending_up</mat-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.averageOpportunityScore | number:'1.0-1' }}</h3>
              <p>Avg Score</p>
              <small>out of 100</small>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Charts Section -->
      <div class="charts-grid" *ngIf="stats">
        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>Posts by Source</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
              [data]="postsBySourceChartData"
              [options]="pieChartOptions"
              [type]="'pie'">
            </canvas>
          </mat-card-content>
        </mat-card>

        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>Opportunities by Status</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
              [data]="opportunitiesByStatusChartData"
              [options]="barChartOptions"
              [type]="'bar'">
            </canvas>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Score Metrics -->
      <mat-card class="metrics-card" *ngIf="stats">
        <mat-card-header>
          <mat-card-title>Opportunity Score Metrics</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="metrics-grid">
            <div class="metric">
              <label>Confidence</label>
              <mat-progress-bar 
                mode="determinate" 
                [value]="stats.averageConfidenceScore"
                color="primary">
              </mat-progress-bar>
              <span>{{ stats.averageConfidenceScore | number:'1.0-1' }}%</span>
            </div>
            <div class="metric">
              <label>Market Potential</label>
              <mat-progress-bar 
                mode="determinate" 
                [value]="stats.averageMarketPotentialScore"
                color="accent">
              </mat-progress-bar>
              <span>{{ stats.averageMarketPotentialScore | number:'1.0-1' }}%</span>
            </div>
            <div class="metric">
              <label>Feasibility</label>
              <mat-progress-bar 
                mode="determinate" 
                [value]="stats.averageFeasibilityScore"
                color="warn">
              </mat-progress-bar>
              <span>{{ stats.averageFeasibilityScore | number:'1.0-1' }}%</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Top Opportunities -->
      <mat-card class="opportunities-card" *ngIf="topOpportunities.length > 0">
        <mat-card-header>
          <mat-card-title>Top Opportunities</mat-card-title>
          <button mat-button color="primary" routerLink="/opportunities">View All</button>
        </mat-card-header>
        <mat-card-content>
          <div class="opportunity-list">
            <div class="opportunity-item" *ngFor="let opp of topOpportunities" 
                 [routerLink]="['/opportunities', opp.id]">
              <div class="opportunity-info">
                <h4>{{ opp.title }}</h4>
                <p>{{ opp.description | slice:0:100 }}...</p>
                <mat-chip-listbox>
                  <mat-chip [color]="getPriorityColor(opp.priority)" highlighted>
                    {{ opp.priority }}
                  </mat-chip>
                  <mat-chip>{{ opp.status }}</mat-chip>
                  <mat-chip *ngIf="opp.category">{{ opp.category }}</mat-chip>
                </mat-chip-listbox>
              </div>
              <div class="opportunity-score">
                <span class="score" [class.high]="(opp.overallScore || 0) >= 70">
                  {{ opp.overallScore | number:'1.0-0' }}
                </span>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 24px;
    }
    
    .page-header {
      margin-bottom: 24px;
      
      h1 {
        margin: 0;
        font-size: 2rem;
        font-weight: 500;
        color: #333;
      }
      
      .subtitle {
        margin: 8px 0 0;
        color: #666;
      }
    }
    
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .stat-card {
      mat-card-content {
        display: flex;
        align-items: center;
        padding: 16px;
      }
      
      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        mat-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
          color: white;
        }
        
        &.posts { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        &.clusters { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        &.opportunities { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        &.score { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
      }
      
      .stat-info {
        h3 {
          margin: 0;
          font-size: 1.75rem;
          font-weight: 600;
          color: #333;
        }
        
        p {
          margin: 4px 0;
          color: #666;
          font-size: 0.875rem;
        }
        
        small {
          color: #999;
          font-size: 0.75rem;
        }
      }
    }
    
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }
    
    .chart-card {
      mat-card-content {
        padding: 16px;
        height: 300px;
      }
      
      canvas {
        max-height: 260px;
      }
    }
    
    .metrics-card {
      margin-bottom: 24px;
      
      .metrics-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 24px;
        padding: 16px;
      }
      
      .metric {
        label {
          display: block;
          margin-bottom: 8px;
          font-weight: 500;
          color: #555;
        }
        
        mat-progress-bar {
          margin-bottom: 8px;
        }
        
        span {
          font-size: 0.875rem;
          color: #666;
        }
      }
    }
    
    .opportunities-card {
      mat-card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .opportunity-list {
        .opportunity-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 16px;
          border-bottom: 1px solid #eee;
          cursor: pointer;
          transition: background 0.2s;
          
          &:hover {
            background: #f5f5f5;
          }
          
          &:last-child {
            border-bottom: none;
          }
          
          .opportunity-info {
            flex: 1;
            
            h4 {
              margin: 0 0 8px;
              font-size: 1rem;
              color: #333;
            }
            
            p {
              margin: 0 0 12px;
              color: #666;
              font-size: 0.875rem;
            }
            
            mat-chip-listbox {
              display: flex;
              gap: 8px;
            }
          }
          
          .opportunity-score {
            margin-left: 16px;
            
            .score {
              display: inline-block;
              width: 48px;
              height: 48px;
              line-height: 48px;
              text-align: center;
              border-radius: 50%;
              background: #e0e0e0;
              color: #666;
              font-weight: 600;
              font-size: 1rem;
              
              &.high {
                background: #4caf50;
                color: white;
              }
            }
          }
        }
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  topOpportunities: Opportunity[] = [];
  isLoading = true;

  // Chart data
  postsBySourceChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{ data: [] }]
  };

  opportunitiesByStatusChartData: ChartData<'bar'> = {
    labels: ['Draft', 'Validated', 'In Progress', 'Implemented'],
    datasets: [{
      data: [0, 0, 0, 0],
      backgroundColor: ['#9e9e9e', '#4caf50', '#2196f3', '#ff9800']
    }]
  };

  pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false
  };

  barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1
        }
      }
    }
  };

  constructor(
    private dashboardService: DashboardService,
    private opportunityService: OpportunityService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadTopOpportunities();
  }

  loadDashboardData(): void {
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.updateCharts();
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
      }
    });
  }

  loadTopOpportunities(): void {
    this.opportunityService.getHighScoringOpportunities(5).subscribe({
      next: (opportunities) => {
        this.topOpportunities = opportunities;
      },
      error: (error) => {
        console.error('Error loading top opportunities:', error);
      }
    });
  }

  updateCharts(): void {
    if (this.stats) {
      // Posts by source chart
      this.postsBySourceChartData = {
        labels: Object.keys(this.stats.postsBySource),
        datasets: [{
          data: Object.values(this.stats.postsBySource),
          backgroundColor: ['#3f51b5', '#e91e63', '#9c27b0', '#00bcd4', '#4caf50', '#ff9800']
        }]
      };

      // Opportunities by status chart
      this.opportunitiesByStatusChartData = {
        labels: ['Draft', 'Validated', 'In Progress', 'Implemented'],
        datasets: [{
          data: [
            this.stats.opportunitiesByStatusDraft,
            this.stats.opportunitiesByStatusValidated,
            this.stats.opportunitiesByStatusInProgress,
            this.stats.opportunitiesByStatusImplemented
          ],
          backgroundColor: ['#9e9e9e', '#4caf50', '#2196f3', '#ff9800']
        }]
      };
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return 'warn';
      case 'HIGH': return 'accent';
      case 'MEDIUM': return 'primary';
      default: return '';
    }
  }
}
