import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MasterDataService, DepartmentMaster, RefPersonMaster, RelatedToMaster } from '../../../services/master-data.service';
import { ToastService } from '../../../services/toast.service';
import { Constants } from '../../../utils/constant';

type MasterTab = 'department' | 'ref_person' | 'related_to';

@Component({
  selector: 'app-master-data',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './master-data.component.html',
  styleUrls: ['./master-data.component.css']
})
export class MasterDataComponent implements OnInit {
  activeTab: MasterTab = Constants.MASTER_DATA.TABS.DEPARTMENT as MasterTab;
  pageSizeOptions = Constants.PAGINATION.OPTIONS;

  // State per tab
  page: number = Constants.PAGINATION.DEFAULT_OFFSET;
  pageSize: number = Constants.PAGINATION.DEFAULT_LIMIT;
  search: string = '';
  totalElements: number = 0;
  totalPages: number = 0;
  isLoading: boolean = false;

  // Data lists
  departmentItems: DepartmentMaster[] = [];
  refPersonItems: RefPersonMaster[] = [];
  relatedToItems: RelatedToMaster[] = [];

  // Modal State
  isModalOpen: boolean = false;
  isEditMode: boolean = false;
  editingId: number | null = null;
  modalName: string = '';
  modalCityVillage: string = '';
  modalState: string = Constants.MASTER_DATA.DEFAULT_STATE;

  constructor(
    private masterDataService: MasterDataService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  setTab(tab: MasterTab): void {
    if (this.activeTab !== tab) {
      this.activeTab = tab;
      this.page = 0;
      this.search = '';
      this.loadData();
    }
  }

  loadData(): void {
    this.isLoading = true;
    if (this.activeTab === 'department') {
      this.masterDataService.getDepartmentMastersPage(this.page, this.pageSize, this.search).subscribe({
        next: res => {
          this.departmentItems = res.content || [];
          this.totalElements = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.toastService.show('Failed to load Department Master data', 'error');
        }
      });
    } else if (this.activeTab === 'ref_person') {
      this.masterDataService.getRefPersonMastersPage(this.page, this.pageSize, this.search).subscribe({
        next: res => {
          this.refPersonItems = res.content || [];
          this.totalElements = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.toastService.show('Failed to load Ref Person Master data', 'error');
        }
      });
    } else if (this.activeTab === 'related_to') {
      this.masterDataService.getRelatedToMastersPage(this.page, this.pageSize, this.search).subscribe({
        next: res => {
          this.relatedToItems = res.content || [];
          this.totalElements = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.toastService.show('Failed to load Related To Master data', 'error');
        }
      });
    }
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadData();
  }

  onPageSizeChange(): void {
    this.page = 0;
    this.loadData();
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadData();
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadData();
    }
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.editingId = null;
    this.modalName = '';
    this.modalCityVillage = '';
    this.modalState = Constants.MASTER_DATA.DEFAULT_STATE;
    this.isModalOpen = true;
  }

  openEditModal(item: any): void {
    this.isEditMode = true;
    this.editingId = item.id;
    this.modalName = item.name || '';
    this.modalCityVillage = item.cityVillage || '';
    this.modalState = item.state || Constants.MASTER_DATA.DEFAULT_STATE;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  saveMasterRecord(): void {
    if (!this.modalName || !this.modalName.trim()) {
      this.toastService.show('Please enter a valid name', 'error');
      return;
    }

    if (this.activeTab === 'department') {
      if (this.isEditMode && this.editingId) {
        this.masterDataService.updateDepartmentMaster(this.editingId, {
          name: this.modalName,
          cityVillage: this.modalCityVillage,
          state: this.modalState || Constants.MASTER_DATA.DEFAULT_STATE
        }).subscribe({
          next: () => {
            this.toastService.show('Department Master updated successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to update Department Master', 'error');
          }
        });
      } else {
        this.masterDataService.createDepartmentMaster({
          name: this.modalName,
          cityVillage: this.modalCityVillage,
          state: this.modalState || Constants.MASTER_DATA.DEFAULT_STATE
        }).subscribe({
          next: () => {
            this.toastService.show('Department Master added successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to add Department Master', 'error');
          }
        });
      }
    } else if (this.activeTab === 'ref_person') {
      if (this.isEditMode && this.editingId) {
        this.masterDataService.updateRefPersonMaster(this.editingId, { name: this.modalName }).subscribe({
          next: () => {
            this.toastService.show('Ref Person Master updated successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to update Ref Person Master', 'error');
          }
        });
      } else {
        this.masterDataService.createRefPersonMaster({ name: this.modalName }).subscribe({
          next: () => {
            this.toastService.show('Ref Person Master added successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to add Ref Person Master', 'error');
          }
        });
      }
    } else if (this.activeTab === 'related_to') {
      if (this.isEditMode && this.editingId) {
        this.masterDataService.updateRelatedToMaster(this.editingId, { name: this.modalName }).subscribe({
          next: () => {
            this.toastService.show('Related To Master updated successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to update Related To Master', 'error');
          }
        });
      } else {
        this.masterDataService.createRelatedToMaster({ name: this.modalName }).subscribe({
          next: () => {
            this.toastService.show('Related To Master added successfully', 'success');
            this.closeModal();
            this.loadData();
          },
          error: err => {
            this.toastService.show(err?.error?.message || 'Failed to add Related To Master', 'error');
          }
        });
      }
    }
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      return d.toLocaleString();
    } catch {
      return dateStr;
    }
  }
}
