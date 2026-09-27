export const RestUrl = {
  API_BASE_URL: 'http://localhost:8080/api',
  USERS: '/users',
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
  NOTIFICATIONS: {
    BASE: '/notifications',
    UNREAD_COUNT: '/notifications/unread-count',
    READ_ALL: '/notifications/read-all',
    READ_SINGLE: (id: number | string) => `/notifications/${id}/read`
  },
  TODOS: '/todos'
};
