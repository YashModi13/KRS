import { Injectable, inject } from '@angular/core';
import { HttpParams, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface ProjectLocation {
  id?: number;
  tenderId?: string;
  villageName?: string;
  taluka?: string;
  district?: string;
  block?: string;
  schoolId?: string;
  schoolName?: string;
  head?: string;
  newAcr?: string;
  newMdmSqm?: string;
  newCwRmt?: string;
  gtb?: string;
  btb?: string;
  cwsnToilet?: string;
  shed?: string;
  repairing?: string;
  status?: string;
  physicalProgress?: number;
  financialProgress?: number;
  timeLimit?: string;
  startDate?: string;
  closedDate?: string;
}

export interface RaBill {
  id?: number;
  raBillNumber?: string;
  dateOfBill?: string;
  nameOfAgency?: string;
  panNumber?: string;
  grossBillAmount?: number;
  totalRaBillAmount?: number;
  totalGst?: number;
  cgst9Percent?: number;
  sgst9Percent?: number;
  netBillAmount?: number;
  rm5Percent?: number;
  tds2Percent?: number;
  labourCess1Percent?: number;
  cgst1Percent?: number;
  sgst1Percent?: number;
  withheldAmount?: number;
  liquidityDamage?: number;
  netPayment?: number;
  invoiceSubmitted?: boolean;
  billCheckPersonName?: string;
  deptRaBillCopyFile?: string;
  billDeposited?: boolean;
  status?: string;
  passedDate?: string;
  paidDate?: string;
  remarks?: string;
}

export interface Approval {
  id?: number;
  approvalType?: string;
  approvalNumber?: string;
  approvalDate?: string;
  amount?: number;
  timeLimitExtensionDate?: string;
  description?: string;
  approvalLetterFile?: string;
  status?: string;
}

export interface Project {
  id?: number;
  dateOfSub?: string;
  departmentName?: string;
  tenderId?: string;
  noticeNo?: string;
  packageNo?: string;
  nameOfWork?: string;
  relatedTo?: string;
  tenderFee?: number;
  tenderFeeNo?: string;
  ddNo?: string;
  emdAmt?: number;
  emdNo?: string;
  estimatedTenderCost?: number;
  tenderedCost?: number;
  aboveBelowPercentage?: number;
  /** Read-only, computed by backend SELECT query */
  variancePct?: number | null;
  refPerson?: string;
  workAwardedStatus?: string;
  workOrderNumber?: string;
  workOrderDate?: string;
  timeLimit?: string;
  timeLimitItems?: any[];
  securityDepositAmount?: number;
  securityDepositDate?: string;
  securityDepositType?: string;
  securityDepositFile?: string;
  sdFdrNo?: string;
  remarks?: string;
  sdRabDeduction?: number;
  sdRabReturnAmount?: number;
  additionalDeduction?: string;
  workCompletedAmount?: number;
  pendingWorkAmount?: number;
  completionDateActual?: string;
  defectsLiabilityPeriod?: string;
  dlpEndedOn?: string;
  emdReturnStatus?: string;
  sdReturnStatus?: string;
  sdRmRabReturnStatus?: string;
  status?: string;
  retentionMoneyPerBill?: number;
  extraExcessAmount?: number;
  timeLimitExtension?: string;
  letterByKrsFile?: string;
  letterByDeptFile?: string;
  locations?: ProjectLocation[];
  raBills?: RaBill[];
  approvals?: Approval[];
}

export interface ProjectResponse {
  data: Project[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly krsService = inject(KrsService);

  getDashboardConfig(): Observable<{ maxTimeLimitDays: number }> {
    return this.krsService.get<{ maxTimeLimitDays: number }>(RestUrl.PROJECTS_CONFIG);
  }

  getAllProjects(limit: number = 10, offset: number = 0, sortBy?: string, sortDir?: string, filters?: any): Observable<ProjectResponse> {
    let params = new HttpParams()
      .set('limit', limit.toString())
      .set('offset', offset.toString());

    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortDir) params = params.set('sortDir', sortDir);

    if (filters) {
      Object.keys(filters).forEach(key => {
        if (filters[key] !== undefined && filters[key] !== null && filters[key] !== '') {
          params = params.set(key, filters[key]);
        }
      });
    }

    return this.krsService.get<ProjectResponse>(RestUrl.PROJECTS, params);
  }

  checkTenderIdExists(tenderId: string, excludeId?: number): Observable<{ exists: boolean }> {
    let params = new HttpParams().set('tenderId', tenderId);
    if (excludeId) {
      params = params.set('excludeId', excludeId.toString());
    }
    return this.krsService.get<{ exists: boolean }>(RestUrl.PROJECT_CHECK_TENDER_ID, params);
  }

  getProjectById(id: number): Observable<Project> {
    return this.krsService.get<Project>(`${RestUrl.PROJECTS}/${id}`);
  }

  createProject(project: Project): Observable<Project> {
    return this.krsService.post<Project>(RestUrl.PROJECTS, project);
  }

  updateProject(id: number, project: Project): Observable<Project> {
    return this.krsService.put<Project>(`${RestUrl.PROJECTS}/${id}`, project);
  }

  deleteProject(id: number): Observable<void> {
    return this.krsService.delete<void>(`${RestUrl.PROJECTS}/${id}`);
  }

  addLocation(projectId: number, location: ProjectLocation): Observable<ProjectLocation> {
    return this.krsService.post<ProjectLocation>(RestUrl.PROJECT_LOCATIONS(projectId), location);
  }

  updateLocation(projectId: number, locationId: number, location: ProjectLocation): Observable<ProjectLocation> {
    return this.krsService.put<ProjectLocation>(RestUrl.PROJECT_LOCATION_DETAIL(projectId, locationId), location);
  }

  deleteLocation(projectId: number, locationId: number): Observable<void> {
    return this.krsService.delete<void>(RestUrl.PROJECT_LOCATION_DETAIL(projectId, locationId));
  }

  // --- Document Management Methods ---
  getProjectDocuments(projectId: number): Observable<ProjectDocument[]> {
    return this.krsService.get<ProjectDocument[]>(RestUrl.PROJECT_DOCUMENTS(projectId));
  }

  uploadProjectDocument(projectId: number, file: File, documentName?: string, notes?: string): Observable<ProjectDocument> {
    const formData = new FormData();
    formData.append('file', file);
    if (documentName) {
      formData.append('documentName', documentName);
    }
    if (notes) {
      formData.append('notes', notes);
    }
    return this.krsService.post<ProjectDocument>(RestUrl.PROJECT_DOCUMENT_UPLOAD(projectId), formData, 'Document uploaded successfully');
  }

  getDownloadDocumentUrl(documentId: number): string {
    return `${RestUrl.API_BASE_URL}${RestUrl.PROJECT_DOCUMENT_DOWNLOAD(documentId)}`;
  }

  deleteProjectDocument(documentId: number): Observable<any> {
    return this.krsService.delete<any>(RestUrl.PROJECT_DOCUMENT_DELETE(documentId), 'Document deleted successfully');
  }

  // --- Bulk Excel Import & Export Methods ---
  private readonly http = inject(HttpClient);

  uploadExcel(file: File): Observable<ProjectUploadHistory> {
    const formData = new FormData();
    formData.append('file', file);
    return this.krsService.post<ProjectUploadHistory>(RestUrl.PROJECTS_EXCEL.UPLOAD, formData, 'Excel processing completed');
  }

  getUploadHistory(): Observable<ProjectUploadHistory[]> {
    return this.krsService.get<ProjectUploadHistory[]>(RestUrl.PROJECTS_EXCEL.HISTORY);
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${RestUrl.API_BASE_URL}${RestUrl.PROJECTS_EXCEL.TEMPLATE}`, { responseType: 'blob' });
  }

  exportProjectsExcel(): Observable<Blob> {
    return this.http.get(`${RestUrl.API_BASE_URL}${RestUrl.PROJECTS_EXCEL.EXPORT}`, { responseType: 'blob' });
  }

  downloadUploadHistoryExport(id: number, type: string = 'ALL'): Observable<Blob> {
    return this.http.get(`${RestUrl.API_BASE_URL}${RestUrl.PROJECTS_EXCEL.HISTORY}/${id}/export?type=${type}`, { responseType: 'blob' });
  }
}

export interface ProjectUploadHistory {
  id?: number;
  filename?: string;
  uploadedBy?: string;
  uploadTime?: string;
  totalRows?: number;
  successCount?: number;
  failedCount?: number;
  status?: string; // 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'
  errorDetails?: string; // JSON string array of row errors
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectDocument {
  id?: number;
  documentName?: string;
  fileName?: string;
  notes?: string;
  uploadedDate?: string;
  locationPath?: string;
  createdDate?: string;
  createdBy?: string;
  updateDate?: string;
  updatedBy?: string;
  isActive?: boolean;
}
