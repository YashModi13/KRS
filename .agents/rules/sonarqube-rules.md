# SonarQube & Code Quality Rules for KRS

This rule file defines mandatory guidelines and patterns to prevent and resolve SonarQube / SonarLint code quality, accessibility, and security warnings across Java backend services, Angular frontend components, and SQL database queries.

---

## 1. Java Backend Rules

### ⚙️ Constants & Dynamic Configuration
- **Static Constants**: Define and retrieve all static application-wide constants, magic numbers, status strings, and default keys through a dedicated constant class (e.g. `Constants.java` / `AppConstants.java`). Avoid hardcoding literals inline across services and controllers.
- **Dynamic & Environment Configuration**: Any dynamic value, environment-dependent setting, or configurable limit (e.g. secret keys, timeout values, file upload directories, external API endpoints) must be defined in `application.properties` (or `application.yml`) and retrieved using Spring's `@Value("${property.key}")` or `@ConfigurationProperties`.

### ⏰ Timezone Specification (`java:S8688`)
- **Rule**: Explicitly specify the time zone or clock when calling date/time `.now()` methods.
- **Pattern**:
  ```java
  // INCORRECT
  LocalDateTime.now();

  // CORRECT
  ZoneId zone = ZoneId.systemDefault();
  LocalDateTime.now(zone);
  ```

### 🔄 Transactional Self-Invocation (`java:S6809`)
- **Rule**: Never invoke a method annotated with `@Transactional` via `this` within the same class, as Spring proxy interceptors will not execute.
- **Pattern**:
  ```java
  // INCORRECT
  @Transactional
  public Entity getOrCreate(String name) {
      return getOrCreate(name, null); // Calling @Transactional method via 'this'
  }

  // CORRECT: Extract implementation to an internal non-transactional private method
  @Transactional
  public Entity getOrCreate(String name) {
      return getOrCreateInternal(name, null);
  }

  @Transactional
  public Entity getOrCreate(String name, String extra) {
      return getOrCreateInternal(name, extra);
  }

  private Entity getOrCreateInternal(String name, String extra) { ... }
  ```

### 🧠 Cognitive Complexity (`java:S3776`)
- **Rule**: Keep Cognitive Complexity of any method strictly below **15**.
- **Remediation Strategy**:
  - Extract row parsing, validation, batch flushing, and entity construction into dedicated single-responsibility helper methods.
  - Avoid deep nesting of `if/else`, `try/catch`, and loops.
  - Replace repeated ternary conditionals inside cell setters or entity builders with helper functions.

### 🚫 Restricted Keywords (`java:S6213`)
- **Rule**: Do not use Java restricted contextual keywords (e.g. `record`, `var`, `yield`) as variable or parameter names.
- **Pattern**:
  ```java
  // INCORRECT
  DepartmentMaster record = repository.findById(id)...;

  // CORRECT
  DepartmentMaster deptRecord = repository.findById(id)...;
  ```

### ⚠️ Specific Exception Handling (`java:S112`)
- **Rule**: Replace generic `throws Exception` or `throws Throwable` declarations with specific library or custom exceptions (e.g. `throws IOException`, `throws JsonProcessingException`, `throws IllegalArgumentException`).
- **Pattern**:
  ```java
  // INCORRECT
  public void processFile(OutputStream out) throws Exception

  // CORRECT
  public void processFile(OutputStream out) throws IOException
  ```

### 📦 Unused Imports (`java:S1128`)
- **Rule**: Remove all unused import statements promptly.

### 💬 Empty Catch Blocks (`java:S108`)
- **Rule**: Never leave a `catch` block empty without a clear comment explaining why the exception is safely ignored.
- **Pattern**:
  ```java
  try {
      username = SecurityContextHolder.getContext().getAuthentication().getName();
  } catch (Exception ignored) {
      // Ignore authentication lookup errors when invoked in unauthenticated background thread
  }
  ```

### 🔀 Nested Ternary Operations (`java:S3358`)
- **Rule**: Extract nested ternary expressions (`cond1 ? val1 : (cond2 ? val2 : val3)`) into clear local variables or `if/else` statements.

---

## 2. Angular (Frontend / CSS / HTML) Rules

### ⚙️ Static Data & REST URL Management
- **Static Constants**: Store all static frontend configurations, default UI options, role names, and dropdown items in dedicated constant files (e.g. `constants.ts` or `app.constants.ts`) and import them where needed. Never hardcode static data arrays/objects directly inside components.
- **REST Endpoints (`rest-url.ts`)**: Maintain all backend API routes, endpoints, and URL paths in a centralized `rest-url.ts` file (or `api-endpoints.ts`). HTTP services must reference endpoints from this file instead of concatenating raw string URLs inside service methods.

### 🎨 Color Contrast & Accessibility (`css:S7924`)
- **Rule**: Ensure text meets minimal WCAG AA contrast requirements against its background color.
- **Gradient Fill Exception**: When using background clipping for text gradients (`background-clip: text; -webkit-text-fill-color: transparent;`), attach `/* NOSONAR */` to the base `color` or `background` declaration to suppress false positive contrast warnings.

### ♿ HTML Form & Keyboard Accessibility
- **Label Association**: Every `<input>`, `<select>`, and `<textarea>` must be associated with a `<label for="element-id">` using matching `id` and `for` attributes.
- **Button Types**: Always specify `type="button"`, `type="submit"`, or `type="reset"` on `<button>` elements to prevent accidental form submission.
- **Interactive Divs**: When adding `(click)` handlers to non-interactive elements (`div`, `span`), also provide `(keydown.enter)` / `(keydown.space)` keyboard event handlers, `tabindex="0"`, and `role="button"`.

---

## 3. SQL & Spring Data JPA Query Rules

### 🛡️ SQL Injection Prevention
- **Rule**: Never concatenate raw input parameters directly into SQL, HQL, or JPQL query strings.
- **Pattern**:
  ```java
  // INCORRECT
  String query = "SELECT p FROM Project p WHERE p.name = '" + input + "'";

  // CORRECT: Use named parameters in JPQL
  @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<Project> searchProjects(@Param("search") String search);

  // CORRECT: Use CriteriaBuilder for dynamic specifications
  builder.like(builder.lower(root.get("name")), "%" + search.toLowerCase() + "%");
  ```

### 🔍 Safe NULL Handling in Queries
- **Rule**: Use explicit `IS NULL` or `IS NOT NULL` clauses instead of equality operators (`= NULL`) in SQL queries.

### 📊 Column Scoping
- **Rule**: Avoid `SELECT *` in native queries when only specific columns are required. Use projection interfaces or explicit DTO constructors.
