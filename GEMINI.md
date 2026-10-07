# KRS Codebase Guidelines & SonarQube Quality Standards

Always follow these mandatory standards for Java, Angular (TypeScript/CSS/HTML), and SQL queries when developing or refactoring code in this repository:

---

## 1. Java Backend Standards

1. **Constants & Application Properties**:
   - Define static application constants in a dedicated constant class (e.g. `Constants.java` / `AppConstants.java`).
   - Define dynamic or environment-dependent values in `application.properties` (or `application.yml`) and inject them via `@Value("${property.key}")` or `@ConfigurationProperties`.

2. **Timezone Specification (`java:S8688`)**:
   - Always pass `ZoneId.systemDefault()` (or `Clock`) to `.now()` methods (e.g. `LocalDateTime.now(ZoneId.systemDefault())`).

3. **Transactional Method Calls (`java:S6809`)**:
   - Never call `@Transactional` methods directly via `this`. Delegate to internal non-transactional private helper methods (`getOrCreateInternal`) so both public entry points share logic safely.

4. **Cognitive Complexity (`java:S3776`)**:
   - Keep Cognitive Complexity strictly below **15**.
   - Modularize validation, entity parsing, batching, and row generation into dedicated private helper functions.

5. **Restricted Identifiers (`java:S6213`)**:
   - Do not use Java 14+ contextual keywords (e.g. `record`, `var`, `yield`) as variable or parameter names. Rename `record` to `deptRecord`, `refRecord`, `relatedRecord`, etc.

6. **Specific Exception Types (`java:S112`)**:
   - Replace generic `throws Exception` or `throws Throwable` declarations with specific exception classes (`IOException`, `JsonProcessingException`, `IllegalArgumentException`).

7. **Empty Catch Blocks (`java:S108`)**:
   - Never leave empty catch blocks. Always include an explanatory comment inside the catch block.

8. **Nested Ternaries (`java:S3358`)**:
   - Extract nested ternary branches into independent `if/else` statements or clear local variables.

---

## 2. Angular Frontend & UI Standards

1. **Constants & REST URL Management**:
   - Maintain static frontend UI data/options in a dedicated constant file (e.g. `constants.ts` or `app.constants.ts`).
   - Maintain all backend REST API routes and endpoints in `rest-url.ts` (or `api-endpoints.ts`) and reference them across HTTP services instead of hardcoding string URLs.

2. **Color Contrast & CSS Gradients (`css:S7924`)**:
   - Maintain WCAG AA minimal text contrast requirements. For gradient text using `-webkit-text-fill-color: transparent;`, annotate with `/* NOSONAR */`.

3. **Accessibility (a11y)**:
   - Associate `<label for="id">` with form controls `<input id="id">`.
   - Specify explicit `type="button"` / `type="submit"` on `<button>` elements.
   - For interactive `div`/`span` elements with `(click)` handlers, add `(keydown.enter)` / `(keydown.space)`, `tabindex="0"`, and `role="button"`.

---

## 3. SQL & Database Query Standards

1. **SQL Injection Prevention**:
   - Never concatenate user parameters into SQL/JPQL queries. Use named parameters (`:param`), positional parameters (`?1`), or JPA Criteria Builder.

2. **NULL Safety**:
   - Use explicit `IS NULL` / `IS NOT NULL` conditions instead of comparison operators (`= NULL`).
