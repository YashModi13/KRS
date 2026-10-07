import { API_BASE_URL } from './constant';

export const RestUrl = {
  API_BASE_URL: API_BASE_URL,

  USERS: '/users',
  USER_THEME: '/users/theme',
  USER_VALIDATION: {
    CHECK_USERNAME: '/users/check-username',
    CHECK_EMAIL: '/users/check-email',
    CHECK_ROLE_NAME: '/roles/check-name'
  },
  ROLES: '/roles',
  PERMISSIONS: '/permissions',
  AUTH: {
    LOGIN: '/auth/login'
  },
  PROJECTS: '/projects',
  PROJECTS_CONFIG: '/projects/config',
  PROJECT_CHECK_TENDER_ID: '/projects/check-tender-id',
  PROJECT_LOCATIONS: (projectId: number | string) => `/projects/${projectId}/locations`,
  PROJECT_LOCATION_DETAIL: (projectId: number | string, locationId: number | string) => `/projects/${projectId}/locations/${locationId}`,
  PROJECT_DOCUMENTS: (projectId: number | string) => `/projects/${projectId}/documents`,
  PROJECT_DOCUMENT_UPLOAD: (projectId: number | string) => `/projects/${projectId}/documents/upload`,
  PROJECT_DOCUMENT_DOWNLOAD: (documentId: number | string) => `/projects/documents/download/${documentId}`,
  PROJECT_DOCUMENT_DELETE: (documentId: number | string) => `/projects/documents/${documentId}`,
  PROJECTS_EXCEL: {
    UPLOAD: '/projects/upload',
    HISTORY: '/projects/upload-history',
    TEMPLATE: '/projects/template',
    EXPORT: '/projects/export/excel'
  },
  NOTIFICATIONS: {
    BASE: '/notifications',
    UNREAD_COUNT: '/notifications/unread-count',
    READ_ALL: '/notifications/read-all',
    READ_SINGLE: (id: number | string) => `/notifications/${id}/read`,
    UNREAD_SINGLE: (id: number | string) => `/notifications/${id}/unread`
  },
  TODOS: '/todos',
  SYSTEM_ERROR_LOGS: `${API_BASE_URL}/admin/error-logs`,
  DEPARTMENT_MASTERS: '/department-masters',
  REF_PERSON_MASTERS: '/ref-person-masters',
  RELATED_TO_MASTERS: '/related-to-masters'
};
