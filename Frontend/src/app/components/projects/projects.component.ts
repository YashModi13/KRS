import { Component, OnInit, inject, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProjectService, Project, ProjectLocation, ProjectDocument, ProjectUploadHistory } from '../../services/project.service';
import { MasterDataService, DepartmentMaster, RefPersonMaster, RelatedToMaster } from '../../services/master-data.service';
import { Constants } from '../../utils/constant';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { ToastService } from '../../services/toast.service';

export type TimeLimitUnit = 'Years' | 'Months' | 'Days';

export interface TimeLimitItem {
  scope: string;
  duration: number | null;
  unit: TimeLimitUnit;
}

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit {
  readonly projectService = inject(ProjectService);
  readonly masterDataService = inject(MasterDataService);
  readonly toastService = inject(ToastService);
  readonly Constants = Constants;

  pageTitle = '';
  pageSubTitle = '';

  mode: 'list' | 'detail' | 'create' | 'edit' | 'upload' = 'list';
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

  readonly Math = Math;

  // Excel Bulk Import & History State
  showExcelUploadModal = false;
  showUploadHistoryModal = false;
  excelFileToUpload: File | null = null;
  isExcelUploading = false;
  uploadResult: ProjectUploadHistory | null = null;
  parsedUploadErrors: string[] = [];
  uploadHistoryList: ProjectUploadHistory[] = [];
  selectedHistoryRecord: ProjectUploadHistory | null = null;
  selectedHistoryErrors: string[] = [];

  // Upload History Pagination State
  historyLimit = Constants.PAGINATION.DEFAULT_LIMIT;
  historyOffset = Constants.PAGINATION.DEFAULT_OFFSET;

  get paginatedUploadHistory(): ProjectUploadHistory[] {
    const sorted = [...this.uploadHistoryList].sort((a, b) => {
      const timeA = a.uploadTime ? new Date(a.uploadTime).getTime() : 0;
      const timeB = b.uploadTime ? new Date(b.uploadTime).getTime() : 0;
      return timeB - timeA; // Descending by upload time
    });
    return sorted.slice(this.historyOffset, this.historyOffset + this.historyLimit);
  }

  get historyTotalPages(): number {
    return Math.ceil(this.uploadHistoryList.length / this.historyLimit) || 1;
  }

  get historyCurrentPage(): number {
    return Math.floor(this.historyOffset / this.historyLimit) + 1;
  }

  get historyShowingText(): string {
    const total = this.uploadHistoryList.length;
    if (total === 0) {
      return 'Showing 0 of 0 records';
    }
    const start = this.historyOffset + 1;
    const end = Math.min(this.historyOffset + this.historyLimit, total);
    return `Showing ${start}–${end} of ${total} records`;
  }

  get isHistoryPrevDisabled(): boolean {
    return this.historyOffset === 0;
  }

  get isHistoryNextDisabled(): boolean {
    return this.historyOffset + this.historyLimit >= this.uploadHistoryList.length;
  }

  onHistoryLimitChange() {
    this.historyOffset = 0;
  }

  prevHistoryPage() {
    if (this.historyOffset >= this.historyLimit) {
      this.historyOffset -= this.historyLimit;
    }
  }

  nextHistoryPage() {
    if (this.historyOffset + this.historyLimit < this.uploadHistoryList.length) {
      this.historyOffset += this.historyLimit;
    }
  }

  projectData: Project = {
    workOrderNumber: '', securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0
  };

  limit = 10;
  offset = 0;
  totalRecords = 0;
  isLoading = false;

  sortBy = '';
  sortDir = 'desc';
  filters: any = { status: '', workAwardedStatus: '' };

  private readonly filterSubject = new Subject<void>();
  private loadSub?: Subscription;

  // Tender ID Uniqueness Check State
  tenderIdExists = false;
  tenderIdChecking = false;
  tenderIdErrorMsg = '';
  private readonly tenderIdSubject = new Subject<string>();

  // Form Wizard & Navigation State
  formStep: number = 1;
  formViewMode: 'wizard' | 'full' = 'wizard';

  setFormStep(step: number) {
    if (step >= 1 && step <= 5) {
      this.formStep = step;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  nextFormStep() {
    if (this.formStep < 5) {
      this.formStep++;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  prevFormStep() {
    if (this.formStep > 1) {
      this.formStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  toggleFormViewMode() {
    this.formViewMode = this.formViewMode === 'wizard' ? 'full' : 'wizard';
  }

  // Master Data Autocomplete State
  departmentMasters: DepartmentMaster[] = [];
  refPersonMasters: RefPersonMaster[] = [];
  relatedToMasters: RelatedToMaster[] = [];

  showDeptDropdown = false;
  showRelatedToDropdown = false;
  showRefPersonDropdown = false;

  get filteredDepartments(): DepartmentMaster[] {
    const search = (this.projectData.departmentName || '').trim().toLowerCase();
    if (!search) return this.departmentMasters;
    return this.departmentMasters.filter(d => d.name?.toLowerCase().includes(search));
  }

  get filteredRelatedTos(): RelatedToMaster[] {
    const search = (this.projectData.relatedTo || '').trim().toLowerCase();
    if (!search) return this.relatedToMasters;
    return this.relatedToMasters.filter(r => r.name?.toLowerCase().includes(search));
  }

  get filteredRefPersons(): RefPersonMaster[] {
    const search = (this.projectData.refPerson || '').trim().toLowerCase();
    if (!search) return this.refPersonMasters;
    return this.refPersonMasters.filter(r => r.name?.toLowerCase().includes(search));
  }

  loadMasterData() {
    this.masterDataService.getAllDepartmentMasters().subscribe({
      next: (data) => this.departmentMasters = data || [],
      error: (err) => console.error('Failed to load department masters', err)
    });

    this.masterDataService.getAllRelatedToMasters().subscribe({
      next: (data) => this.relatedToMasters = data || [],
      error: (err) => console.error('Failed to load related to masters', err)
    });

    this.masterDataService.getAllRefPersonMasters().subscribe({
      next: (data) => this.refPersonMasters = data || [],
      error: (err) => console.error('Failed to load ref person masters', err)
    });
  }

  selectDepartment(dept: DepartmentMaster) {
    this.projectData.departmentName = dept.name;
    this.showDeptDropdown = false;
  }

  createDepartment(name?: string) {
    const cleanName = (name || this.projectData.departmentName || '').trim();
    if (!cleanName) {
      this.toastService.warning('Please enter a department name first.');
      return;
    }
    this.masterDataService.createDepartmentMaster({ name: cleanName }).subscribe({
      next: (created) => {
        this.toastService.success(`Created department "${created.name}"`);
        this.projectData.departmentName = created.name;
        if (!this.departmentMasters.some(d => d.id === created.id)) {
          this.departmentMasters.push(created);
        }
        this.showDeptDropdown = false;
      },
      error: () => {
        this.toastService.error('Failed to create department');
      }
    });
  }

  selectRelatedTo(rel: RelatedToMaster) {
    this.projectData.relatedTo = rel.name;
    this.showRelatedToDropdown = false;
  }

  createRelatedTo(name?: string) {
    const cleanName = (name || this.projectData.relatedTo || '').trim();
    if (!cleanName) {
      this.toastService.warning('Please enter a name first.');
      return;
    }
    this.masterDataService.createRelatedToMaster({ name: cleanName }).subscribe({
      next: (created) => {
        this.toastService.success(`Created "${created.name}"`);
        this.projectData.relatedTo = created.name;
        if (!this.relatedToMasters.some(r => r.id === created.id)) {
          this.relatedToMasters.push(created);
        }
        this.showRelatedToDropdown = false;
      },
      error: () => {
        this.toastService.error('Failed to create item');
      }
    });
  }

  selectRefPerson(ref: RefPersonMaster) {
    this.projectData.refPerson = ref.name;
    this.showRefPersonDropdown = false;
  }

  createRefPerson(name?: string) {
    const cleanName = (name || this.projectData.refPerson || '').trim();
    if (!cleanName) {
      this.toastService.warning('Please enter a reference person name first.');
      return;
    }
    this.masterDataService.createRefPersonMaster({ name: cleanName }).subscribe({
      next: (created) => {
        this.toastService.success(`Created reference person "${created.name}"`);
        this.projectData.refPerson = created.name;
        if (!this.refPersonMasters.some(r => r.id === created.id)) {
          this.refPersonMasters.push(created);
        }
        this.showRefPersonDropdown = false;
      },
      error: () => {
        this.toastService.error('Failed to create reference person');
      }
    });
  }

  onlyNumeric(event: KeyboardEvent): boolean {
    const key = event.key;
    if (event.isComposing || key === 'Backspace' || key === 'Tab' || key === 'ArrowLeft' || key === 'ArrowRight' || key === 'Delete') {
      return true;
    }
    if (!/^\d$/.test(key)) {
      event.preventDefault();
      return false;
    }
    return true;
  }

  onTenderIdInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const sanitized = input.value.replace(/\D/g, '');
      input.value = sanitized;
      this.projectData.tenderId = sanitized;
      if (sanitized.trim()) {
        this.tenderIdSubject.next(sanitized.trim());
      } else {
        this.tenderIdExists = false;
        this.tenderIdErrorMsg = '';
      }
    }
  }

  validateTenderIdUniqueness(tenderId: string): void {
    if (!tenderId?.trim()) {
      this.tenderIdExists = false;
      this.tenderIdErrorMsg = '';
      return;
    }
    this.tenderIdChecking = true;
    this.projectService.checkTenderIdExists(tenderId.trim(), this.projectData.id).subscribe({
      next: (res) => {
        this.tenderIdChecking = false;
        if (res.exists) {
          this.tenderIdExists = true;
          this.tenderIdErrorMsg = `Tender ID "${tenderId}" already exists! Tender ID must be unique.`;
        } else {
          this.tenderIdExists = false;
          this.tenderIdErrorMsg = '';
        }
      },
      error: () => {
        this.tenderIdChecking = false;
      }
    });
  }

  onNoticeNoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const upper = input.value.toUpperCase();
      input.value = upper;
      this.projectData.noticeNo = upper;
    }
  }

  onFilterNoticeNoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const upper = input.value.toUpperCase();
      input.value = upper;
      this.filters['noticeNo'] = upper;
      this.onSearchInput();
    }
  }

  ngOnInit() {
    this.pageTitle = Constants.PROJECTS.PAGE_TITLE;
    this.pageSubTitle = Constants.PROJECTS.PAGE_SUBTITLE;

    this.filterSubject.pipe(
      debounceTime(400)
    ).subscribe(() => {
      this.applyFilters();
    });

    this.tenderIdSubject.pipe(
      debounceTime(350)
    ).subscribe(tenderId => {
      this.validateTenderIdUniqueness(tenderId);
    });

    this.loadProjects();
    this.loadMasterData();
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
    if (type === 'excel') {
      this.exportExcel();
    } else {
      alert(`Exporting data to ${type.toUpperCase()}... (Preparing file export from database)`);
    }
  }

  // --- Excel Bulk Import & History Handlers ---

  openExcelUploadModal() {
    this.excelFileToUpload = null;
    this.uploadResult = null;
    this.parsedUploadErrors = [];
    this.mode = 'upload';
    this.loadUploadHistory();
  }

  closeExcelUploadModal() {
    this.mode = 'list';
    this.excelFileToUpload = null;
  }

  onExcelFileSelected(event: any) {
    const file = event.target.files?.[0];
    if (file) {
      this.excelFileToUpload = file;
    }
  }

  uploadExcelFile() {
    if (!this.excelFileToUpload) {
      this.toastService.warning('Please select an Excel file (.xlsx) first.');
      return;
    }
    this.isExcelUploading = true;
    this.uploadResult = null;
    this.parsedUploadErrors = [];

    this.projectService.uploadExcel(this.excelFileToUpload).subscribe({
      next: (res) => {
        this.isExcelUploading = false;
        this.uploadResult = res;
        if (res.errorDetails) {
          try {
            this.parsedUploadErrors = JSON.parse(res.errorDetails);
          } catch (e) {
            this.parsedUploadErrors = [res.errorDetails];
          }
        }
        if (res.successCount && res.successCount > 0) {
          this.toastService.success(`Successfully imported ${res.successCount} projects!`);
          this.loadProjects(); // Refresh list after successful import
        } else if (res.failedCount && res.failedCount > 0) {
          this.toastService.warning(`Upload completed with ${res.failedCount} failed/skipped rows.`);
        }
        this.loadUploadHistory(); // Instantly refresh full-page history table
      },
      error: (err) => {
        this.isExcelUploading = false;
        console.error('Upload failed', err);
        const errMsg = err.error?.message || err.error || err.message || 'Server processing error during Excel upload';
        this.parsedUploadErrors = [`Upload Failed: ${errMsg}`];
        this.toastService.error('Excel upload failed: ' + errMsg);
      }
    });
  }

  downloadTemplate() {
    this.projectService.downloadTemplate().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'Tender_Details_Template.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.success('Template downloaded successfully');
      },
      error: (err) => {
        console.error('Failed to download template', err);
        this.toastService.error('Failed to download template');
      }
    });
  }

  exportExcel() {
    this.exportMenuOpen = false;
    this.toastService.info('Preparing streaming Excel export...');
    this.projectService.exportProjectsExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'Projects_Master_Export.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.success('Projects exported to Excel successfully');
      },
      error: (err) => {
        console.error('Failed to export projects', err);
        this.toastService.error('Failed to export projects to Excel');
      }
    });
  }

  openUploadHistoryModal() {
    this.mode = 'upload';
    this.loadUploadHistory();
  }

  closeUploadHistoryModal() {
    this.mode = 'list';
    this.selectedHistoryRecord = null;
    this.selectedHistoryErrors = [];
  }

  loadUploadHistory() {
    this.projectService.getUploadHistory().subscribe({
      next: (res) => {
        this.uploadHistoryList = res;
      },
      error: (err) => {
        console.error('Failed to fetch upload history', err);
      }
    });
  }

  getHistoryErrorReasonTooltip(item: ProjectUploadHistory): string {
    if (!item.errorDetails) {
      if (item.failedCount && item.failedCount > 0) {
        return `Failure / Skipped Reasons:\n• ${item.failedCount} row(s) were skipped due to duplication or invalid format.`;
      }
      return 'Upload failed. Click 🔍 Logs for detailed report.';
    }
    try {
      const parsed = JSON.parse(item.errorDetails);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return `Reason for Failure / Skipped Rows:\n• ` + parsed.join('\n• ');
      }
      return `Reason for Failure:\n${item.errorDetails}`;
    } catch {
      // Ignore JSON parse error if errorDetails is plain text instead of JSON format
      return `Reason for Failure:\n${item.errorDetails}`;
    }
  }

  viewHistoryDetails(record: ProjectUploadHistory) {
    this.selectedHistoryRecord = record;
    if (record.errorDetails) {
      try {
        this.selectedHistoryErrors = JSON.parse(record.errorDetails);
      } catch {
        // Ignore JSON parse error if errorDetails is plain text instead of JSON format
        this.selectedHistoryErrors = [record.errorDetails];
      }
    } else {
      this.selectedHistoryErrors = [];
    }
  }

  downloadHistoryExport(record: ProjectUploadHistory, type: string = 'ALL') {
    if (!record.id) return;
    this.toastService.info(`Preparing ${type.toUpperCase()} records download...`);
    this.projectService.downloadUploadHistoryExport(record.id, type).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Upload_History_${record.id}_${type.toUpperCase()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.success(`${type.toUpperCase()} records downloaded successfully`);
      },
      error: (err) => {
        console.error('Failed to download history export', err);
        this.toastService.error('Failed to download history export file');
      }
    });
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
    const target = event.target as HTMLElement;
    if (!target.closest('.autocomplete-container')) {
      this.showDeptDropdown = false;
      this.showRelatedToDropdown = false;
      this.showRefPersonDropdown = false;
    }
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
    this.loadMasterData();
    this.formStep = 1;
    this.tenderIdExists = false;
    this.tenderIdChecking = false;
    this.tenderIdErrorMsg = '';
    this.projectData = {
      workOrderNumber: '', departmentName: '', tenderId: '', noticeNo: '', nameOfWork: '',
      relatedTo: '', refPerson: '', workAwardedStatus: 'Running', estimatedTenderCost: 0,
      tenderedCost: 0, securityDepositAmount: 0, retentionMoneyPerBill: 0, extraExcessAmount: 0
    };
    this.initTimeLimitItemsFromProject();
    this.mode = 'create';
  }

  editProject(project: Project, event: Event) {
    event.stopPropagation();
    this.loadMasterData();
    this.formStep = 1;
    this.tenderIdExists = false;
    this.tenderIdChecking = false;
    this.tenderIdErrorMsg = '';
    this.projectData = { ...project };
    this.initTimeLimitItemsFromProject(project);
    this.mode = 'edit';

    if (project.id) {
      this.projectService.getProjectById(project.id).subscribe({
        next: (fullProject) => {
          this.projectData = { ...fullProject };
          this.initTimeLimitItemsFromProject(fullProject);
        }
      });
    }
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
      villageName: '', block: '', schoolId: '', schoolName: '', head: '', repairing: '',
      newAcr: '', newMdmSqm: '', newCwRmt: '', gtb: '', btb: '', cwsnToilet: '', shed: '',
      status: 'Running', physicalProgress: 0, financialProgress: 0, startDate: '', closedDate: ''
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

  // Cost Validation & Auto-calculation Handlers
  onCostChange() {
    const est = Number(this.projectData.estimatedTenderCost) || 0;
    const ted = Number(this.projectData.tenderedCost) || 0;
    if (est > 0 && ted > 0) {
      const pct = ((ted - est) / est) * 100;
      this.projectData.aboveBelowPercentage = Number(pct.toFixed(2));
    }
  }

  onAboveBelowChange() {
    const est = Number(this.projectData.estimatedTenderCost) || 0;
    const pct = Number(this.projectData.aboveBelowPercentage) || 0;
    if (est > 0) {
      const ted = est * (1 + (pct / 100));
      this.projectData.tenderedCost = Number(ted.toFixed(2));
    }
  }

  hasCostInput(): boolean {
    const est = Number(this.projectData?.estimatedTenderCost) || 0;
    const ted = Number(this.projectData?.tenderedCost) || 0;
    return est > 0 || ted > 0;
  }

  isCostExceeded(): boolean {
    const est = Number(this.projectData?.estimatedTenderCost) || 0;
    const ted = Number(this.projectData?.tenderedCost) || 0;
    if (ted > 0 && (est <= 0 || ted > est)) {
      return true;
    }
    return false;
  }

  getCostDifferencePercentage(): string {
    const est = Number(this.projectData?.estimatedTenderCost) || 0;
    const ted = Number(this.projectData?.tenderedCost) || 0;
    if (est <= 0) {
      return ted > 0 ? 'N/A (Est. Cost is ₹0)' : '0.00%';
    }
    const pct = Math.abs(((ted - est) / est) * 100);
    return pct.toFixed(2) + '%';
  }

  getCostDifferenceAmount(): number {
    const est = Number(this.projectData?.estimatedTenderCost) || 0;
    const ted = Number(this.projectData?.tenderedCost) || 0;
    return Math.abs(ted - est);
  }

  // --- Time Limit Table & Target Close Date Calculation ---
  readonly timeLimitUnits = Constants.TIME_LIMIT.UNITS;

  timeLimitItems: TimeLimitItem[] = [];

  addTimeLimitRow() {
    this.timeLimitItems.push({ scope: '', duration: null, unit: 'Months' });
    this.updateProjectTimeLimitSummary();
  }

  removeTimeLimitRow(index: number) {
    if (this.timeLimitItems.length > 0) {
      this.timeLimitItems.splice(index, 1);
      this.updateProjectTimeLimitSummary();
    }
  }

  getTotalTimeLimitYears(): number {
    return this.timeLimitItems
      .filter(item => item.unit === 'Years' && item.duration)
      .reduce((sum, item) => sum + (Number(item.duration) || 0), 0);
  }

  getTotalTimeLimitMonths(): number {
    return this.timeLimitItems
      .filter(item => item.unit === 'Months' && item.duration)
      .reduce((sum, item) => sum + (Number(item.duration) || 0), 0);
  }

  getTotalTimeLimitDays(): number {
    return this.timeLimitItems
      .filter(item => item.unit === 'Days' && item.duration)
      .reduce((sum, item) => sum + (Number(item.duration) || 0), 0);
  }

  getTargetCloseDate(): Date | null {
    if (!this.projectData.workOrderDate) {
      return null;
    }
    const startDate = new Date(this.projectData.workOrderDate);
    if (Number.isNaN(startDate.getTime())) {
      return null;
    }
    const totalYears = this.getTotalTimeLimitYears();
    const totalMonths = this.getTotalTimeLimitMonths();
    const totalDays = this.getTotalTimeLimitDays();

    const target = new Date(startDate);
    if (totalYears > 0) {
      target.setFullYear(target.getFullYear() + totalYears);
    }
    if (totalMonths > 0) {
      target.setMonth(target.getMonth() + totalMonths);
    }
    if (totalDays > 0) {
      target.setDate(target.getDate() + totalDays);
    }
    return target;
  }

  getFormattedTargetCloseDate(): string {
    const target = this.getTargetCloseDate();
    if (!target) {
      return 'Select Work Order Date to calculate target close date';
    }
    const yyyy = target.getFullYear();
    const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const monthStr = monthNames[target.getMonth()];
    const dd = String(target.getDate()).padStart(2, '0');
    return `${dd} ${monthStr} ${yyyy}`;
  }

  private buildTimeLimitSummaryString(parts: string[], totalStr: string, targetDateStr: string): string {
    let summary = parts.join('; ');
    if (summary) {
      summary += ` (Total: ${totalStr}`;
      if (this.projectData.workOrderDate && targetDateStr && !targetDateStr.includes('Select')) {
        summary += ` | Target Close: ${targetDateStr}`;
      }
      summary += `)`;
    } else {
      summary = `Total Time Limit: ${totalStr}`;
    }
    return summary;
  }

  updateProjectTimeLimitSummary() {
    if (!this.timeLimitItems || this.timeLimitItems.length === 0) {
      this.projectData.timeLimit = '';
      this.projectData.timeLimitItems = [];
      return;
    }
    this.projectData.timeLimitItems = this.timeLimitItems.map((item, idx) => ({
      scope: item.scope,
      duration: item.duration,
      unit: item.unit,
      sortOrder: idx + 1
    }));

    const parts = this.timeLimitItems
      .filter(i => i.scope && i.duration)
      .map(i => `${i.scope} - ${i.duration} ${i.unit}`);

    const totalYears = this.getTotalTimeLimitYears();
    const totalMonths = this.getTotalTimeLimitMonths();
    const totalDays = this.getTotalTimeLimitDays();
    const totals: string[] = [];
    if (totalYears > 0) totals.push(`${totalYears} ${totalYears === 1 ? 'Year' : 'Years'}`);
    if (totalMonths > 0) totals.push(`${totalMonths} ${totalMonths === 1 ? 'Month' : 'Months'}`);
    if (totalDays > 0) totals.push(`${totalDays} ${totalDays === 1 ? 'Day' : 'Days'}`);

    const totalStr = totals.length > 0 ? totals.join(', ') : '0 Days';
    const targetDateStr = this.getFormattedTargetCloseDate();
    this.projectData.timeLimit = this.buildTimeLimitSummaryString(parts, totalStr, targetDateStr);

    const target = this.getTargetCloseDate();
    if (target) {
      const yyyy = target.getFullYear();
      const mm = String(target.getMonth() + 1).padStart(2, '0');
      const dd = String(target.getDate()).padStart(2, '0');
      this.projectData.completionDateActual = `${yyyy}-${mm}-${dd}`;
    }
  }

  private parseSingleTimeLimitRow(trimmed: string): TimeLimitItem {
    const dashIdx = trimmed.lastIndexOf('-');
    if (dashIdx > 0) {
      const scope = trimmed.substring(0, dashIdx).trim();
      const right = trimmed.substring(dashIdx + 1).trim();
      const durMatch = /^(\d+)\s*([a-zA-Z]+)$/.exec(right);
      if (durMatch) {
        const duration = Number.parseInt(durMatch[1], 10);
        const unitRaw = durMatch[2].toLowerCase();
        let unit: TimeLimitUnit = 'Months';
        if (unitRaw.startsWith('year')) unit = 'Years';
        else if (unitRaw.startsWith('day')) unit = 'Days';
        return { scope, duration, unit };
      }
    }

    const digitsOnly = trimmed.replace(/\D/g, '');
    if (digitsOnly) {
      const duration = Number.parseInt(digitsOnly, 10);
      const lower = trimmed.toLowerCase();
      let unit: TimeLimitUnit = 'Months';
      if (lower.includes('year')) unit = 'Years';
      else if (lower.includes('day')) unit = 'Days';
      return { scope: 'General Scope', duration, unit };
    }

    return { scope: trimmed, duration: null, unit: 'Months' };
  }

  initTimeLimitItemsFromProject(project?: Project) {
    this.timeLimitItems = [];
    if (project?.timeLimitItems && project.timeLimitItems.length > 0) {
      this.timeLimitItems = project.timeLimitItems.map(item => ({
        scope: item.scope || '',
        duration: item.duration || null,
        unit: (item.unit as TimeLimitUnit) || 'Months'
      }));
      this.updateProjectTimeLimitSummary();
      return;
    }

    const timeLimitStr = project?.timeLimit;
    if (!timeLimitStr?.trim()) {
      const def = Constants.TIME_LIMIT.DEFAULT_ITEM;
      this.timeLimitItems = [{ scope: def.scope, duration: def.duration, unit: def.unit as TimeLimitUnit }];
      this.updateProjectTimeLimitSummary();
      return;
    }

    const cleanStr = timeLimitStr.replace(/\(Total:.*\)/i, '').trim();
    const rows = cleanStr.split(';');
    for (const r of rows) {
      const trimmed = r.trim();
      if (trimmed) {
        this.timeLimitItems.push(this.parseSingleTimeLimitRow(trimmed));
      }
    }

    if (this.timeLimitItems.length === 0) {
      const def = Constants.TIME_LIMIT.DEFAULT_ITEM;
      this.timeLimitItems = [{ scope: def.scope, duration: def.duration, unit: def.unit as TimeLimitUnit }];
    }
    this.updateProjectTimeLimitSummary();
  }

  saveProject() {
    this.updateProjectTimeLimitSummary();
    if (this.tenderIdExists) {
      this.toastService.error(this.tenderIdErrorMsg || 'Tender ID already exists. Please enter a unique Tender ID.');
      return;
    }
    if (this.isCostExceeded()) {
      const est = Number(this.projectData.estimatedTenderCost) || 0;
      const ted = Number(this.projectData.tenderedCost) || 0;
      this.toastService.error(
        Constants.MESSAGES.VALIDATION?.TENDER_COST_EXCEEDED ||
        `Validation Error: Tendered Cost (₹${ted.toLocaleString('en-IN')}) cannot exceed Estimated Tender Cost (₹${est.toLocaleString('en-IN')}).`
      );
      return;
    }
    if (this.projectData.tenderId) {
      this.projectData.tenderId = this.projectData.tenderId.toString().replace(/\D/g, '');
    }
    if (this.projectData.noticeNo) {
      this.projectData.noticeNo = this.projectData.noticeNo.toUpperCase();
    }
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
