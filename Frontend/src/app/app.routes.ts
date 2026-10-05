import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProjectsComponent } from './components/projects/projects.component';
import { RaBillsComponent } from './components/ra-bills/ra-bills.component';
import { ApprovalsComponent } from './components/approvals/approvals.component';
import { EotComponent } from './components/eot/eot.component';
import { VisitsComponent } from './components/visits/visits.component';
import { HindranceComponent } from './components/hindrance/hindrance.component';
import { MaterialsComponent } from './components/materials/materials.component';
import { PaymentsComponent } from './components/payments/payments.component';
import { CompletionComponent } from './components/completion/completion.component';
import { UserManagementComponent } from './components/admin/user-management/user-management.component';
import { RoleManagementComponent } from './components/admin/role-management/role-management.component';
import { SettingsComponent } from './components/settings/settings.component';
import { DailyTasksComponent } from './components/daily-tasks/daily-tasks.component';
import { SystemErrorLogsComponent } from './components/admin/system-error-logs/system-error-logs.component';
import { MasterDataComponent } from './components/admin/master-data/master-data.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'daily-tasks', component: DailyTasksComponent, canActivate: [authGuard] },
  { path: 'projects', component: ProjectsComponent, canActivate: [authGuard] },
  { path: 'ra-bills', component: RaBillsComponent, canActivate: [authGuard] },
  { path: 'approvals', component: ApprovalsComponent, canActivate: [authGuard] },
  { path: 'eot', component: EotComponent, canActivate: [authGuard] },
  { path: 'visits', component: VisitsComponent, canActivate: [authGuard] },
  { path: 'hindrance', component: HindranceComponent, canActivate: [authGuard] },
  { path: 'materials', component: MaterialsComponent, canActivate: [authGuard] },
  { path: 'payments', component: PaymentsComponent, canActivate: [authGuard] },
  { path: 'completion', component: CompletionComponent, canActivate: [authGuard] },
  { path: 'admin/users', component: UserManagementComponent, canActivate: [authGuard] },
  { path: 'admin/roles', component: RoleManagementComponent, canActivate: [authGuard] },
  { path: 'admin/master-data', component: MasterDataComponent, canActivate: [authGuard] },
  { path: 'admin/error-logs', component: SystemErrorLogsComponent, canActivate: [authGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [authGuard] },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];
