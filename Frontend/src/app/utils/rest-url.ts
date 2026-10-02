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
  NOTIFICATIONS: {
    BASE: '/notifications',
    UNREAD_COUNT: '/notifications/unread-count',
    READ_ALL: '/notifications/read-all',
    READ_SINGLE: (id: number | string) => `/notifications/${id}/read`,
    UNREAD_SINGLE: (id: number | string) => `/notifications/${id}/unread`
  },
  TODOS: '/todos'
};
