import { Component, OnInit, inject, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProjectService, Project, ProjectLocation, ProjectDocument } from '../../services/project.service';
import { Constants } from '../../utils/constant';
import { Subject, Subscription } from 'rxjs';
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
  readonly projectService = inject(ProjectService);
  readonly toastService = inject(ToastService);
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
  private loadSub?: Subscription;

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
    this.loadSub?.unsubscribe();
    this.loadSub = this.projectService.getAllProjects(this.limit, this.offset, this.sortBy, this.sortDir, this.filters).subscribe({
      next: (res) => {
        this.projects = res.data;
        this.totalRecords = res.total;
        
        this.projects.forEach(p => {
          if (!p.status) {
            if (p.workAwardedStatus === 'Work Completed') {
              p.status = 'Completed';
            } else if (p.workAwardedStatus === 'Not') {
              p.status = 'Not Awarded';
            } else {
              p.status = 'Running';
            }
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
    const currentOffset = Number(this.offset);
    const currentLimit = Number(this.limit);
    const end = Math.min(currentOffset + currentLimit, this.totalRecords);
    return `Showing ${currentOffset + 1} to ${end} of ${this.totalRecords}`;
  }

  get isPrevDisabled(): boolean {
    return Number(this.offset) <= 0;
  }

  get isNextDisabled(): boolean {
    return Number(this.offset) + Number(this.limit) >= this.totalRecords;
  }

  onLimitChange(newLimit: any) {
    this.limit = Number(newLimit);
    this.offset = 0;
    this.loadProjects();
  }

  nextPage() {
    const currentOffset = Number(this.offset);
    const currentLimit = Number(this.limit);
    if (currentOffset + currentLimit < this.totalRecords) {
      this.offset = currentOffset + currentLimit;
      this.loadProjects();
    }
  }

  prevPage() {
    const currentOffset = Number(this.offset);
    const currentLimit = Number(this.limit);
    if (currentOffset >= currentLimit) {
      this.offset = currentOffset - currentLimit;
    } else {
      this.offset = 0;
    }
    this.loadProjects();
  }


  getVariance(p: Project | null): { text: string, cls: string, icon: string, tip: string } {
    let value: number | null = null;
    if (p?.variancePct !== undefined && p?.variancePct !== null) {
      value = Number(p.variancePct);
    } else if (p?.estimatedTenderCost && p?.tenderedCost) {
      // Fallback for records not yet saved (e.g. create/edit form)
      value = ((p.tenderedCost - p.estimatedTenderCost) / p.estimatedTenderCost) * 100;
    }

    if (value === null || Number.isNaN(value)) {
      return { text: 'N/A', cls: 'variance-na', icon: '', tip: 'Estimated / tendered cost not available' };
    }
    if (Math.abs(value) < 0.005) {
      return { text: 'At Par', cls: 'variance-par', icon: '●', tip: 'Tendered cost equals estimated cost' };
    }

    const abs = Math.abs(value).toFixed(2);
    return value < 0
      ? { text: `-${abs}% Below`, cls: 'variance-below', icon: '▼', tip: `Tendered ${abs}% below estimate` }
      : { text: `+${abs}% Above`, cls: 'variance-above', icon: '▲', tip: `Tendered ${abs}% above estimate` };
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
    if (!project?.locations?.length) return 0;
    const total = project.locations.reduce((acc, loc) => acc + (loc.physicalProgress || 0), 0);
    return Math.round(total / project.locations.length);
  }

  getFinancialProgress(project: Project | null): number {
    if (!project?.tenderedCost || project.tenderedCost <= 0) return 0;
    const totalPaid = (project.raBills || [])
      .filter(b => b.status === 'Paid' || b.billDeposited || b.status === 'paid')
      .reduce((acc, b) => acc + (b.netPayment || b.grossBillAmount || 0), 0);
    const pct = Math.round((totalPaid / project.tenderedCost) * 100);
    return Math.min(pct, 100);
  }

  getDaysRemaining(endDateStr?: string): { days: number, label: string, isOverdue: boolean } | null {
    if (!endDateStr) return null;
    const end = new Date(endDateStr).getTime();
    const now = Date.now();
    const diffDays = Math.ceil((end - now) / (1000 * 3600 * 24));
    if (diffDays < 0) {
      return { days: Math.abs(diffDays), label: `${Math.abs(diffDays)} Days Overdue`, isOverdue: true };
    } else {
      return { days: diffDays, label: `${diffDays} Days Remaining`, isOverdue: false };
    }
  }

  getTotalRaBilled(project: Project | null): number {
    return project?.raBills?.reduce((acc, b) => acc + (b.grossBillAmount || 0), 0) ?? 0;
  }

  getTotalRaPaid(project: Project | null): number {
    return project?.raBills?.reduce((acc, b) => acc + (b.netPayment || 0), 0) ?? 0;
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
      this.projectData.locations ??= [];
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
