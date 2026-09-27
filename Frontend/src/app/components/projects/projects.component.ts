import { Component, OnInit, inject, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProjectService, Project } from '../../services/project.service';
import { Constants } from '../../utils/constant';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit {
  projectService = inject(ProjectService);
  
  pageTitle = '';
  pageSubTitle = '';

  mode: 'list' | 'detail' | 'create' = 'list';
  projects: Project[] = [];
  selectedProject: Project | null = null;
  saveSuccess = false;

  projectData: Project = {
    workOrderNumber: '', villageName: '', securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0
  };

  limit = 10;
  offset = 0;
  totalRecords = 0;
  isLoading = false;

  sortBy = '';
  sortDir = 'asc';
  filters: any = { status: '' };

  private filterSubject = new Subject<void>();

  ngOnInit() {
    this.pageTitle = Constants.PROJECTS.PAGE_TITLE;
    this.pageSubTitle = Constants.PROJECTS.PAGE_SUBTITLE;
    
    this.filterSubject.pipe(
      debounceTime(400)
    ).subscribe(() => {
      this.applyFilters();
    });

    this.loadProjects();
  }

  onSearchInput() {
    this.filterSubject.next();
  }

  exportToCSV() {
    this.exportData('csv');
  }

  toggleExportMenu(event: Event) {
    event.stopPropagation();
    this.exportMenuOpen = !this.exportMenuOpen;
    this.activeFilterMenu = null;
  }

  exportData(type: string) {
    this.exportMenuOpen = false;
    alert(`Exporting data to ${type.toUpperCase()}... (This feature will be connected to the backend soon!)`);
  }

  loadProjects() {
    this.isLoading = true;
    this.projectService.getAllProjects(this.limit, this.offset, this.sortBy, this.sortDir, this.filters).subscribe({
      next: (res) => {
        this.projects = res.data;
        this.totalRecords = res.total;
        
        this.projects.forEach(p => {
          const id = p.id || 0;
          const statuses = ['Running', 'Running', 'Delayed', 'Completed', 'Not Started', 'Hold'];
          (p as any)._status = statuses[id % statuses.length];
          (p as any)._daysLeft = -Math.abs((id * 43) % 600);
        });
        
        // Sort active first and closer last
        const statusOrder: any = { 'Running': 1, 'Delayed': 2, 'Hold': 3, 'Not Started': 4, 'Completed': 5 };
        this.projects.sort((a: any, b: any) => statusOrder[a._status] - statusOrder[b._status]);

        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load projects', err);
        this.isLoading = false;
      }
    });
  }

  onSort(column: string) {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    this.offset = 0;
    this.loadProjects();
  }

  activeFilterMenu: string | null = null;
  exportMenuOpen: boolean = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    this.activeFilterMenu = null;
    this.exportMenuOpen = false;
  }

  toggleFilter(column: string, event: Event) {
    event.stopPropagation();
    this.activeFilterMenu = this.activeFilterMenu === column ? null : column;
  }

  applyFilters() {
    this.activeFilterMenu = null;
    this.offset = 0;
    this.loadProjects();
  }

  clearFilter(column: string) {
    delete this.filters[column];
    delete this.filters[column + 'Min'];
    delete this.filters[column + 'Max'];
    this.applyFilters();
  }

  onFilterChange(column: string, event: any) {
    this.filters[column] = event.target.value;
    // Don't auto-reload here, let them press Apply in the popup
  }

  get showingText() {
    if (this.totalRecords === 0) return 'Showing 0 to 0 of 0';
    const end = Math.min(this.offset + Number(this.limit), this.totalRecords);
    return `Showing ${this.offset + 1} to ${end} of ${this.totalRecords}`;
  }

  onLimitChange(newLimit: any) {
    this.limit = Number(newLimit);
    this.offset = 0;
    this.loadProjects();
  }

  nextPage() {
    if (this.offset + Number(this.limit) < this.totalRecords) {
      this.offset += Number(this.limit);
      this.loadProjects();
    }
  }

  prevPage() {
    if (this.offset >= Number(this.limit)) {
      this.offset -= Number(this.limit);
      this.loadProjects();
    }
  }

  getAboveBelow(tenderAmt: number | undefined, agreementAmt: number | undefined): { text: string, class: string } {
    if (!tenderAmt || !agreementAmt) return { text: 'N/A', class: 'text-grey' };
    const diff = agreementAmt - tenderAmt;
    if (diff === 0) return { text: 'At Par', class: 'text-grey' };
    const percent = (diff / tenderAmt) * 100;
    if (percent < 0) {
      return { text: `${percent.toFixed(1)}% Below`, class: 'text-green' };
    } else {
      return { text: `+${percent.toFixed(1)}% Above`, class: 'text-red' };
    }
  }

  viewDetails(id: number) {
    this.isLoading = true;
    this.projectService.getProjectById(id).subscribe({
      next: (data) => {
        this.selectedProject = data;
        this.mode = 'detail';
        this.isLoading = false;
      },
      error: (err) => {
        alert('Failed to load project details');
        this.isLoading = false;
      }
    });
  }

  saveProject() {
    this.projectService.createProject(this.projectData).subscribe({
      next: (res) => {
        this.saveSuccess = true;
        this.loadProjects();
        setTimeout(() => {
          this.saveSuccess = false;
          this.mode = 'list';
        }, 1500);
        this.projectData = { workOrderNumber: '', villageName: '', securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0 };
      },
      error: (err) => alert('Failed to save project')
    });
  }
}
