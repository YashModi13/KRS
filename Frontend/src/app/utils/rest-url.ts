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
