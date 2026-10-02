import { Component, OnInit, inject, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProjectService, Project, ProjectLocation, ProjectDocument } from '../../services/project.service';
import { Constants } from '../../utils/constant';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit {
  projectService = inject(ProjectService);
  toastService = inject(ToastService);
  readonly Constants = Constants;
  
  pageTitle = '';
  pageSubTitle = '';

  mode: 'list' | 'detail' | 'create' | 'edit' = 'list';
  activeTab: 'overview' | 'locations' | 'rabills' | 'approvals' | 'milestones' | 'team' | 'documents' = 'overview';
  projects: Project[] = [];
  selectedProject: Project | null = null;
  saveSuccess = false;

  // Project Documents State
  projectDocuments: ProjectDocument[] = [];
  showUploadModal = false;
  selectedFileToUpload: File | null = null;
  uploadDocumentName = '';
  uploadNotes = '';
  isUploading = false;

  projectData: Project = {
    workOrderNumber: '', villageName: '', securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0
  };

  limit = 10;
  offset = 0;
  totalRecords = 0;
  isLoading = false;

  sortBy = '';
  sortDir = 'desc';
  filters: any = { status: '', workAwardedStatus: '' };

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

  toggleExportMenu(event: Event) {
    event.stopPropagation();
    this.exportMenuOpen = !this.exportMenuOpen;
    this.activeFilterMenu = null;
  }

  exportData(type: string) {
    this.exportMenuOpen = false;
    alert(`Exporting data to ${type.toUpperCase()}... (Preparing file export from database)`);
  }

  loadProjects() {
    this.isLoading = true;
    this.projectService.getAllProjects(this.limit, this.offset, this.sortBy, this.sortDir, this.filters).subscribe({
      next: (res) => {
        this.projects = res.data;
        this.totalRecords = res.total;
        
        this.projects.forEach(p => {
          if (!p.status) {
            p.status = p.workAwardedStatus === 'Work Completed' ? 'Completed' : (p.workAwardedStatus === 'Not' ? 'Not Awarded' : 'Running');
          }
        });

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

  getAboveBelow(tenderAmt: number | undefined, agreementAmt: number | undefined, pct?: number): { text: string, class: string } {
    if (pct !== undefined && pct !== null) {
      if (pct === 0) return { text: 'At Par', class: 'text-grey' };
      if (pct < 0) return { text: `${pct}% Below`, class: 'text-green' };
      return { text: `+${pct}% Above`, class: 'text-red' };
    }
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

  setTab(tab: 'overview' | 'locations' | 'rabills' | 'approvals' | 'milestones' | 'team' | 'documents') {
    this.activeTab = tab;
    if (tab === 'documents' && this.selectedProject?.id) {
      this.loadProjectDocuments(this.selectedProject.id);
    }
  }

  viewDetails(id: number) {
    this.isLoading = true;
    this.projectService.getProjectById(id).subscribe({
      next: (data) => {
        this.selectedProject = data;
        this.activeTab = 'overview';
        this.mode = 'detail';
        this.isLoading = false;
        this.loadProjectDocuments(id);
      },
      error: (err) => {
        this.toastService.error('Failed to load project details');
        this.isLoading = false;
      }
    });
  }

  // --- Document Handling Methods ---
  loadProjectDocuments(projectId: number) {
    this.projectService.getProjectDocuments(projectId).subscribe({
      next: (docs) => {
        this.projectDocuments = docs;
      },
      error: () => {
        this.projectDocuments = [];
      }
    });
  }

  openUploadModal() {
    this.selectedFileToUpload = null;
    this.uploadDocumentName = '';
    this.uploadNotes = '';
    this.showUploadModal = true;
  }

  closeUploadModal() {
    this.showUploadModal = false;
    this.selectedFileToUpload = null;
    this.uploadDocumentName = '';
    this.uploadNotes = '';
  }

  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      const file = event.target.files[0];
      this.selectedFileToUpload = file;
      if (!this.uploadDocumentName && file) {
        this.uploadDocumentName = file.name;
      }
    }
  }

  submitUploadDocument() {
    if (!this.selectedProject?.id) {
      this.toastService.error('No project selected');
      return;
    }
    if (!this.selectedFileToUpload) {
      this.toastService.error('Please choose a file to upload');
      return;
    }

    this.isUploading = true;
    this.projectService.uploadProjectDocument(this.selectedProject.id, this.selectedFileToUpload, this.uploadDocumentName, this.uploadNotes).subscribe({
      next: (newDoc) => {
        this.isUploading = false;
        this.closeUploadModal();
        this.loadProjectDocuments(this.selectedProject!.id!);
      },
      error: (err) => {
        this.isUploading = false;
      }
    });
  }

  downloadDocument(doc: ProjectDocument) {
    if (!doc.id) return;
    const downloadUrl = this.projectService.getDownloadDocumentUrl(doc.id);
    window.open(downloadUrl, '_blank');
  }

  deleteDocument(docId: number | undefined, event: Event) {
    event.stopPropagation();
    if (!docId) return;
    if (confirm('Are you sure you want to delete this document?')) {
      this.projectService.deleteProjectDocument(docId).subscribe({
        next: () => {
          if (this.selectedProject?.id) {
            this.loadProjectDocuments(this.selectedProject.id);
          }
        }
      });
    }
  }

  getPhysicalProgress(project: Project | null): number {
    if (!project || !project.locations || project.locations.length === 0) return 0;
    const total = project.locations.reduce((acc, loc) => acc + (loc.physicalProgress || 0), 0);
    return Math.round(total / project.locations.length);
  }

  getFinancialProgress(project: Project | null): number {
    if (!project || !project.tenderedCost || project.tenderedCost <= 0) return 0;
    const totalPaid = (project.raBills || [])
      .filter(b => b.status === 'Paid' || b.billDeposited || b.status === 'paid')
      .reduce((acc, b) => acc + (b.netPayment || b.grossBillAmount || 0), 0);
    const pct = Math.round((totalPaid / project.tenderedCost) * 100);
    return Math.min(pct, 100);
  }

  getDaysRemaining(endDateStr?: string): { days: number, label: string, isOverdue: boolean } | null {
    if (!endDateStr) return null;
    const end = new Date(endDateStr).getTime();
    const now = new Date().getTime();
    const diffDays = Math.ceil((end - now) / (1000 * 3600 * 24));
    if (diffDays < 0) {
      return { days: Math.abs(diffDays), label: `${Math.abs(diffDays)} Days Overdue`, isOverdue: true };
    } else {
      return { days: diffDays, label: `${diffDays} Days Remaining`, isOverdue: false };
    }
  }

  getTotalRaBilled(project: Project | null): number {
    if (!project || !project.raBills) return 0;
    return project.raBills.reduce((acc, b) => acc + (b.grossBillAmount || 0), 0);
  }

  getTotalRaPaid(project: Project | null): number {
    if (!project || !project.raBills) return 0;
    return project.raBills.reduce((acc, b) => acc + (b.netPayment || 0), 0);
  }

  openCreateMode() {
    this.projectData = {
      workOrderNumber: '', departmentName: '', tenderId: '', noticeNo: '', nameOfWork: '',
      relatedTo: '', refPerson: '', workAwardedStatus: 'Running', estimatedTenderCost: 0,
      tenderedCost: 0, securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0
    };
    this.mode = 'create';
  }

  editProject(project: Project, event: Event) {
    event.stopPropagation();
    this.projectData = { ...project };
    this.mode = 'edit';
  }

  deleteProject(id: number, event: Event) {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this project?')) {
      this.projectService.deleteProject(id).subscribe({
        next: () => {
          this.loadProjects();
        },
        error: (err) => alert('Failed to delete project')
      });
    }
  }

  // Location sub-sites management
  showLocationModal = false;
  locationModalMode: 'add' | 'edit' = 'add';
  editingLocation: ProjectLocation = {};

  openAddLocationModal() {
    this.editingLocation = {
      villageName: '', block: '', schoolName: '', head: '', status: 'Running',
      physicalProgress: 0, financialProgress: 0, startDate: '', closedDate: ''
    };
    this.locationModalMode = 'add';
    this.showLocationModal = true;
  }

  openEditLocationModal(loc: ProjectLocation, event: Event) {
    event.stopPropagation();
    this.editingLocation = { ...loc };
    this.locationModalMode = 'edit';
    this.showLocationModal = true;
  }

  closeLocationModal() {
    this.showLocationModal = false;
  }

  recalculateProjectDatesLocally(project: Project) {
    if (project.locations && project.locations.length > 0) {
      let minStart: string | null = null;
      let maxClosed: string | null = null;

      project.locations.forEach(loc => {
        if (loc.startDate) {
          if (!minStart || loc.startDate < minStart) {
            minStart = loc.startDate;
          }
        }
        if (loc.closedDate) {
          if (!maxClosed || loc.closedDate > maxClosed) {
            maxClosed = loc.closedDate;
          }
        }
      });

      if (minStart) project.workOrderDate = minStart;
      if (maxClosed) project.completionDateActual = maxClosed;
    }
  }

  saveLocationModal() {
    if (this.mode === 'detail' && this.selectedProject?.id) {
      if (this.locationModalMode === 'add') {
        this.projectService.addLocation(this.selectedProject.id, this.editingLocation).subscribe({
          next: () => {
            this.toastService.success('Sub-site location added. Project dates auto-updated!');
            this.showLocationModal = false;
            this.viewDetails(this.selectedProject!.id!);
          },
          error: () => this.toastService.error('Failed to add location')
        });
      } else if (this.editingLocation.id) {
        this.projectService.updateLocation(this.selectedProject.id, this.editingLocation.id, this.editingLocation).subscribe({
          next: () => {
            this.toastService.success('Sub-site location updated. Project dates auto-updated!');
            this.showLocationModal = false;
            this.viewDetails(this.selectedProject!.id!);
          },
          error: () => this.toastService.error('Failed to update location')
        });
      }
    } else if (this.mode === 'create' || this.mode === 'edit') {
      if (!this.projectData.locations) {
        this.projectData.locations = [];
      }
      if (this.locationModalMode === 'add') {
        this.projectData.locations.push({ ...this.editingLocation });
      } else {
        const idx = this.projectData.locations.findIndex(l => l.id === this.editingLocation.id);
        if (idx >= 0) {
          this.projectData.locations[idx] = { ...this.editingLocation };
        }
      }
      this.recalculateProjectDatesLocally(this.projectData);
      this.showLocationModal = false;
    }
  }

  deleteLocationSite(locId: number | undefined, index: number, event: Event) {
    event.stopPropagation();
    if (!confirm('Are you sure you want to remove this location sub-site?')) return;

    if (this.mode === 'detail' && this.selectedProject?.id && locId) {
      this.projectService.deleteLocation(this.selectedProject.id, locId).subscribe({
        next: () => {
          this.toastService.success('Sub-site location removed. Project dates recalculated!');
          this.viewDetails(this.selectedProject!.id!);
        },
        error: () => this.toastService.error('Failed to delete location')
      });
    } else if (this.mode === 'create' || this.mode === 'edit') {
      if (this.projectData.locations) {
        this.projectData.locations.splice(index, 1);
        this.recalculateProjectDatesLocally(this.projectData);
      }
    }
  }

  saveProject() {
    if (this.mode === 'edit' && this.projectData.id) {
      this.projectService.updateProject(this.projectData.id, this.projectData).subscribe({
        next: (res) => {
          this.saveSuccess = true;
          this.loadProjects();
          setTimeout(() => {
            this.saveSuccess = false;
            this.mode = 'list';
          }, 1200);
        },
        error: (err) => alert('Failed to update project')
      });
    } else {
      this.projectService.createProject(this.projectData).subscribe({
        next: (res) => {
          this.saveSuccess = true;
          this.loadProjects();
          setTimeout(() => {
            this.saveSuccess = false;
            this.mode = 'list';
          }, 1200);
        },
        error: (err) => alert('Failed to save project')
      });
    }
  }
}
