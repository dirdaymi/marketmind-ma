import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'posts',
    loadComponent: () => import('./features/posts/post-list/post-list.component').then(m => m.PostListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'posts/:id',
    loadComponent: () => import('./features/posts/post-detail/post-detail.component').then(m => m.PostDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'clusters',
    loadComponent: () => import('./features/clusters/cluster-list/cluster-list.component').then(m => m.ClusterListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'clusters/:id',
    loadComponent: () => import('./features/clusters/cluster-detail/cluster-detail.component').then(m => m.ClusterDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'opportunities',
    loadComponent: () => import('./features/opportunities/opportunity-list/opportunity-list.component').then(m => m.OpportunityListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'opportunities/:id',
    loadComponent: () => import('./features/opportunities/opportunity-detail/opportunity-detail.component').then(m => m.OpportunityDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'opportunities/create',
    loadComponent: () => import('./features/opportunities/opportunity-form/opportunity-form.component').then(m => m.OpportunityFormComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ANALYST', 'ADMIN'] }
  },
  {
    path: 'analytics',
    loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ANALYST', 'ADMIN'] }
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: '**',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent)
  }
];
