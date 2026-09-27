# KRS Construction Management System

An enterprise full-stack web application designed for construction company management, tender tracking, site project monitoring, Running Account (RA) billing, and multi-level approval workflows.

---

## 🏗️ Architecture & Technology Stack

| Component          | Technology                     | Description                                                    |
| :----------------- | :----------------------------- | :------------------------------------------------------------- |
| **Frontend** | Angular                        | Standalone components, RxJS, Reactive Forms, HTTP Interceptors |
| **Backend**  | Java 21 / Spring Boot 4.2.0-M1 | REST APIs, Spring Data JPA, Spring Security                    |
| **Database** | PostgreSQL                     | `krs_db` with Liquibase automated database migrations        |
| **Security** | JWT (JSON Web Tokens)          | Token-based stateless authentication & RBAC                    |
| **Scripts**  | PowerShell & Windows Batch     | Scaffolding scripts and single-command application launcher    |

---

## 📁 Project Directory Structure

```text
c:\Projects\KRS\
├── Backend/                   # Spring Boot REST API
│   ├── src/main/java/com/krs/backend/
│   │   ├── controllers/      # REST API (Auth, User, Role, Tender, etc.)
│   │   ├── models/           # JPA Entities (User, Role, PageMaster, Notifications, etc.)
│   │   ├── repositories/     # Spring Data JPA Repositories
│   │   ├── services/         # Business Logic (PermissionService, etc.)
│   │   └── security/         # JWT Security Configuration & Filters
│   └── src/main/resources/   # Application properties & Liquibase changelogs
├── Frontend/                  # Angular SPA Application
│   └── src/app/
│       ├── components/
│       │   ├── admin/        # User & Role Management (Premium Data Tables)
│       │   ├── login/        # JWT Authentication UI
│       │   ├── dashboard/    # Global Overview Metrics
│       │   ├── projects/     # Project & Site Monitoring
│       │   ├── ra-bills/     # RA Bill Generation & Tracking
│       │   └── approvals/    # Multi-Tier Approval Workflows
│       ├── services/         # API HTTP Services & AuthInterceptor
│       ├── utils/            # Constants & REST URLs configurations
│       └── app.routes.ts     # Route Definitions
├── SQL/                      # Core DB schemas, Python generators & Test Data
├── Client Data/              # Client reference assets (PDFs, mockups, bill samples)
└── start_project.bat         # Startup script for launching full-stack app
```

---

## 🚀 Key Modules & Capabilities

1. **Enterprise Identity & Access Management (IAM)**
   - **User Management**: Premium UI data tables with real-time filtering, active/inactive toggles, dynamic role assignments, and duplicate validation.
   - **Role & Permission Management**: Custom role creation with active/inactive states. Supports deep, granular page and action mapping (RolePageActionMapping & UserPageActionMapping).
   - **Authentication**: Stateless JWT token authentication with Spring Security 6+.

2. **Tender & Project Lifecycle**
   - **Tender Management**: Track active & submitted tenders, including values, client specs, and deadlines.
   - **Construction Monitoring**: Track ongoing sites, project budgets, and locations.

3. **Running Account (RA) Billing Engine**
   - Digital RA bill creation with itemized tracking.
   - Deduction calculations and automated bill lifecycle management.

4. **Multi-Tier Approval Workflows**
   - Granular approval routing for Site Managers, Billing Engineers, and Executives.
   - Fully linked to RA Bills and generic daily tasks.

5. **Real-time Alerting & Audit**
   - **Notifications**: Integrated notification engine (`User_Notification_Read`) to alert users to pending approvals or billing updates.
   - **Audit Trails**: Global database triggers and `audit_logs` tracking for all generic CREATE, UPDATE, and DELETE actions.

---

## ⚙️ Setup & Installation Guide

### Prerequisites

- Java 21 LTS (`java -version 21+`)
- Maven 3.9+
- Node.js (v18+) & Angular CLI (`@angular/cli`)
- PostgreSQL 14+

### 1. Database Initialization

Execute the setup script [`SQL/schema.sql`](file:///c:/Projects/KRS/SQL/schema.sql) in PostgreSQL:

Step 1: Run against default database

```sql
CREATE USER krd_postgress WITH PASSWORD 'Krs@Prod!Db#2026';
CREATE DATABASE krs_db WITH OWNER krd_postgress;
```

Step 2: Connect to `krs_db` and run the schema setup:

```sql
CREATE SCHEMA IF NOT EXISTS krs_schema AUTHORIZATION krd_postgress;
GRANT ALL PRIVILEGES ON DATABASE krs_db TO krd_postgress;
GRANT ALL PRIVILEGES ON SCHEMA krs_schema TO krd_postgress;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA krs_schema TO krd_postgress;
ALTER DEFAULT PRIVILEGES IN SCHEMA krs_schema GRANT ALL ON TABLES TO krd_postgress;
```

*Database migrations (including the new `audit_logs` table and table indexes) will run automatically via Liquibase when the backend starts.*

### 2. Running the Application

Launch both backend and frontend concurrently with the included batch script:

```cmd
start_project.bat
```

Alternatively, start them separately:

- **Backend**:
  ```cmd
  cd Backend
  mvn spring-boot:run
  ```
- **Frontend**:
  ```cmd
  cd Frontend
  npm start
  ```

### 3. Default Credentials

After launching the frontend application (typically at `http://localhost:4200`), you can log in using the superadmin account seeded by Liquibase:

- **Username:** `superadmin`
- **Password:** `admin123`

---

## 📝 Change Log & Project Updates

| Date                 | Category                      | Summary of Changes                                                                                                                                                                                            | Author    |
| :------------------- | :---------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :-------- |
| **2026-09-26** | **Framework Upgrade**   | Updated Spring Boot parent version to**`4.2.0-M1`** in `pom.xml`, configured Spring Milestone repositories, and modernized `WebSecurityConfig` to Security 6+ Lambda DSL syntax.                  | Yash Modi |
| **2026-09-26** | **JDK Upgrade**         | Upgraded Backend project to**JDK 21** (`release: 21`, `maven-compiler-plugin 3.11.0`). Fixed UTF-8 BOM encoding issues in Java source files and updated generator scripts.                          | Yash Modi |
| **2026-09-26** | **Documentation**       | Created initial master project documentation (`README.md`) detailing architecture, modules, DB schema setup, and runtime execution instructions.                                                            | Yash Modi |
| **2026-09-26** | **Database Updates**    | Migrated schema from`public` to `krs_schema`. Added manual Liquibase tracking tables with search indexes. Implemented `audit_logs` table for tracking generic action changes (CREATE, UPDATE, DELETE).  | Yash Modi |
| **2026-09-26** | **Backend System**      | Replaced default Spring Boot Logback engine with high-performance Log4j2 engine. Enabled Liquibase execution summary and custom logging output.                                                               | Yash Modi |
| **2026-09-26** | **Bug Fixes & Scripts** | Fixed Angular component syntax errors and updated`tsconfig.json` for JSON imports. Fixed Liquibase changelog formatting. Enhanced `start_project.bat` to automatically kill stale processes on port 8080. | Yash Modi |
| **2026-09-27** | **User Management**     | Implemented full User Management module with premium UI, including an 'Add User' modal, real-time searching/filtering, pagination, dynamic role assignment, and validation for duplicate usernames/emails.  | Yash Modi |
| **2026-09-27** | **Role Management**     | Upgraded Role Management to match premium User UI. Added real-time filtering, instant name validation, and an `is_active` status toggle with visual badges and backend database schema enhancements.        | Yash Modi |
| **2026-09-27** | **Version Control**     | Initialized Git repository, added `.gitignore`, and pushed the full KRS codebase to remote GitHub repository (`https://github.com/YashModi13/KRS.git`).                                                   | Yash Modi |

*(Note: Whenever new features, bug fixes, or architecture modifications are made, update this section with the details).*
