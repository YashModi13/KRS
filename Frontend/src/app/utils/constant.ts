// Toggle flag: true for local backend (http://localhost:8081/api), false for Render production backend (https://krs-backend-uv4l.onrender.com/api)
export const IS_LOCAL_ENVIRONMENT = false;

export const BACKEND_URLS = {
  LOCAL: 'http://localhost:8081/api',
  PRODUCTION: 'https://krs-backend-uv4l.onrender.com/api'
};

export const API_BASE_URL = IS_LOCAL_ENVIRONMENT ? BACKEND_URLS.LOCAL : BACKEND_URLS.PRODUCTION;

export const Constants = {

  PAGINATION: {
    DEFAULT_LIMIT: 10,
    DEFAULT_OFFSET: 0,
    OPTIONS: [10, 25, 50, 100]
  },
  THEME: {
    LIGHT: 'light',
    DARK: 'dark',
    DEFAULT: 'light'
  },
  APP: {
    TITLE: "KRS Construction Project Management",
    NAME: "KRS Construction",
    SUBTITLE: "Gujarat PWD",
    VERSION: "1.0.0",
    DEFAULT_LANGUAGE: "en"
  },
  PROJECTS: {
    PAGE_TITLE: "Projects Repository",
    PAGE_SUBTITLE: "Comprehensive Master Record of Tenders, Agreements, and Status (Active & Completed)",
    TIME_LIMIT_WARNING_DAYS: 45,
    STATUS_OPTIONS: ["Running", "Completed", "Work Completed", "Delayed", "Hold", "Not Started"],
    CONSTRUCTION_TYPES: ["School", "Hospital", "Road", "Bridge", "Residential"]
  },
  MASTER_DATA: {
    DEFAULT_STATE: "Gujarat",
    TABS: {
      DEPARTMENT: "department",
      REF_PERSON: "ref_person",
      RELATED_TO: "related_to"
    }
  },
  PROJECT_OFFICIALS: {
    EXECUTIVE_ENGINEER: {
      TITLE: "Executive Engineer (PWD)",
      NAME: "Ramesh Patel (EE)",
      PHONE: "+91 98765 43210"
    },
    DEPUTY_EXECUTIVE_ENGINEER: {
      TITLE: "Deputy Executive Engineer",
      NAME: "Suresh Dave (DEE)",
      PHONE: "+91 97654 32109"
    }
  },
  USER_MANAGEMENT: {
    CREDENTIALS_SHARE_TEMPLATE: "Hello,\n\nHere are your login credentials for {{APP_NAME}}:\n\nLogin URL: {{URL}}\nUsername: {{USERNAME}}\nEmail: {{EMAIL}}\nPassword: {{PASSWORD}}\nRoles: {{ROLES}}\n\nPlease keep this secure."
  },
  MESSAGES: {
    HTTP_ERRORS: {
      STATUS_0: "Cannot connect to server. Please check your connection.",
      STATUS_401: "Session expired. Please login again.",
      STATUS_403: "You do not have permission to perform this action.",
      STATUS_404: "The requested resource was not found.",
      STATUS_409: "A conflict occurred. The record may already exist.",
      STATUS_500: "Server error. Please try again later.",
      DEFAULT_ERROR: "Something went wrong. Please try again."
    },
    SUCCESS: {
      DEFAULT_CREATE: "Record created successfully!",
      DEFAULT_UPDATE: "Record updated successfully!",
      DEFAULT_DELETE: "Record deleted successfully!",
      PASSWORD_CHANGED_SELF: "Your password was updated successfully. Please login again with your new password.",
      PROFILE_CHANGED_SELF: "Your profile was updated successfully. Please login again to apply the changes.",
      LOGIN_SUCCESS: "Login successful for {{USERNAME}} [{{ROLE}}]",
      LOGOUT_SUCCESS: "Sign out complete for {{USERNAME}}"
    }
  }
};
