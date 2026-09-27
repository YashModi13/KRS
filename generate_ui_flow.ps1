$appDir = "c:\Projects\KRS\Frontend\src\app"
$compDir = "$appDir\components"

New-Item -ItemType Directory -Force -Path "$compDir\projects"
New-Item -ItemType Directory -Force -Path "$compDir\ra-bills"
New-Item -ItemType Directory -Force -Path "$compDir\approvals"

@"
import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <h1 class="page-title">Dashboard</h1>
    <p class="page-sub">Project Management Overview</p>
    
    <div class="kpi-grid">
      <div class="kpi-card red">
        <div class="kpi-label">Time Limit Notification</div>
        <div class="kpi-value">3 Projects <span style="font-size:12px;color:red">(< 45 days left)</span></div>
      </div>
      <div class="kpi-card yellow">
        <div class="kpi-label">RM & SD Release Notifications</div>
        <div class="kpi-value">5 Pending</div>
      </div>
    </div>
    
    <div class="card">
      <div class="card-hdr">Project Status & Daily Tasks</div>
      <div class="card-body">
        <p style="color:var(--muted); margin-bottom:15px">Daily tasks updated by project engineers with remarks.</p>
        
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Task Description</th>
                <th>Assigned To</th>
                <th>Status</th>
                <th>Remarks</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Concrete pouring block A</td>
                <td>Raj Patel</td>
                <td><input type="checkbox" checked disabled></td>
                <td>Completed on time</td>
              </tr>
              <tr>
                <td>Site Inspection Detroj</td>
                <td>Amit Shah</td>
                <td><input type="checkbox" disabled></td>
                <td>Pending review</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent {}
"@ | Out-File -FilePath "$compDir\dashboard\dashboard.component.ts" -Encoding UTF8

@"
import { Component } from '@angular/core';

@Component({
  selector: 'app-projects',
  standalone: true,
  template: `
    <h1 class="page-title">Ongoing Projects</h1>
    <p class="page-sub">Track active work orders, multiple village sites, and extensions</p>
    
    <div class="card">
      <div class="card-hdr">Project Detail Registration</div>
      <div class="card-body">
        <div class="grid grid-3">
          <div class="form-group">
            <label>Work Order Number / Negotiation Letter</label>
            <input type="text" placeholder="Upload Letter or Enter No.">
          </div>
          <div class="form-group">
            <label>Village Name (Supports Multiple)</label>
            <input type="text" placeholder="Add Village Sites with Time Limits">
          </div>
          <div class="form-group">
            <label>Security Deposit (FDR / Bank Guarantee)</label>
            <input type="text" placeholder="Amount and Upload Option">
          </div>
          <div class="form-group">
            <label>Retention Money Per Bill</label>
            <input type="number" placeholder="Total Tracked Automatically">
          </div>
          <div class="form-group">
            <label>Extra Excess Amount</label>
            <input type="number" placeholder="Excess Approved">
          </div>
          <div class="form-group">
            <label>Time Limit Extension</label>
            <input type="date" placeholder="Extended Date">
          </div>
          <div class="form-group">
            <label>Completion Date (Actual vs Extended)</label>
            <input type="text" placeholder="Comparison Display">
          </div>
          <div class="form-group">
            <label>Letters by KRS / Department</label>
            <input type="file" placeholder="Upload options with name">
          </div>
        </div>
        <div style="margin-top:20px">
          <button class="btn btn-primary">Save Project Detail</button>
        </div>
      </div>
    </div>
  `
})
export class ProjectsComponent {}
"@ | Out-File -FilePath "$compDir\projects\projects.component.ts" -Encoding UTF8

@"
import { Component } from '@angular/core';

@Component({
  selector: 'app-ra-bills',
  standalone: true,
  template: `
    <h1 class="page-title">RA Bills Status</h1>
    <p class="page-sub">Manage and track running account bills</p>
    
    <div class="card">
      <div class="card-hdr">RA Bills Tracking</div>
      <div class="card-body">
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Project Name</th>
                <th>Invoice Submitted</th>
                <th>Checked By Person</th>
                <th>Dept. RA Bill Copy</th>
                <th>Bill Deposited (Yes/No)</th>
                <th>Total of All RA Bills</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Navyug Primary School</td>
                <td><span class="badge badge-success">Submitted</span></td>
                <td>Priya Desai</td>
                <td><button class="btn btn-secondary">View Copy</button></td>
                <td><span class="badge badge-success">Yes</span></td>
                <td>₹ 4,500,000</td>
              </tr>
              <tr>
                <td>Gandhi Primary School</td>
                <td><span class="badge badge-warning">Pending</span></td>
                <td>Amit Shah</td>
                <td><button class="btn btn-secondary">Upload</button></td>
                <td><span class="badge badge-danger">No</span></td>
                <td>₹ 1,200,000</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class RaBillsComponent {}
"@ | Out-File -FilePath "$compDir\ra-bills\ra-bills.component.ts" -Encoding UTF8

@"
import { Component } from '@angular/core';

@Component({
  selector: 'app-approvals',
  standalone: true,
  template: `
    <h1 class="page-title">Approvals (Manjuri)</h1>
    <p class="page-sub">Track extra work, excess amounts, and time extensions</p>
    
    <div class="card">
      <div class="card-hdr">Approval Workflows</div>
      <div class="card-body">
        <div class="grid grid-2">
          
          <div class="card" style="margin-bottom:0">
            <div class="card-hdr" style="background:#f8fafc">Extra Work / Excess Amount</div>
            <div class="card-body">
              <div class="form-group">
                <label>Requested Excess Amount</label>
                <input type="number" placeholder="Enter amount">
              </div>
              <button class="btn btn-primary">Submit for Approval</button>
            </div>
          </div>
          
          <div class="card" style="margin-bottom:0">
            <div class="card-hdr" style="background:#f8fafc">Time Limit Extension</div>
            <div class="card-body">
              <div class="form-group">
                <label>Extension Up To Date</label>
                <input type="date">
              </div>
              <p style="font-size:12px; color:var(--muted); margin-bottom:10px;">
                * This data is also connected with the Ongoing Project panel.
              </p>
              <button class="btn btn-primary">Request Extension</button>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  `
})
export class ApprovalsComponent {}
"@ | Out-File -FilePath "$compDir\approvals\approvals.component.ts" -Encoding UTF8

@"
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProjectsComponent } from './components/projects/projects.component';
import { RaBillsComponent } from './components/ra-bills/ra-bills.component';
import { ApprovalsComponent } from './components/approvals/approvals.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'projects', component: ProjectsComponent },
  { path: 'ra-bills', component: RaBillsComponent },
  { path: 'approvals', component: ApprovalsComponent },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
];
"@ | Out-File -FilePath "$appDir\app.routes.ts" -Encoding UTF8
