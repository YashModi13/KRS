# Java Development Rules

- **No Field Injection (`@Autowired`)**: Always use constructor-based dependency injection for Spring components (`@RestController`, `@Service`, `@Component`, `@Configuration`).
- Declare injected dependencies as `private final` fields.
