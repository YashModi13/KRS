$backendDir = "c:\Projects\KRS\Backend\src\main\java\com\krs\backend"
$modelsDir = "$backendDir\models"
$repoDir = "$backendDir\repositories"
$secDir = "$backendDir\security"
$ctrlDir = "$backendDir\controllers"

New-Item -ItemType Directory -Force -Path $modelsDir
New-Item -ItemType Directory -Force -Path $repoDir
New-Item -ItemType Directory -Force -Path $secDir
New-Item -ItemType Directory -Force -Path $ctrlDir

@"
package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}
"@ | Out-File -FilePath "$modelsDir\User.java" -Encoding ascii

@"
package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
}
"@ | Out-File -FilePath "$modelsDir\Role.java" -Encoding ascii

@"
package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tenders")
@Data
public class Tender {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenderNo;
    private String name;
    private String description;
    private BigDecimal tenderAmount;
    private BigDecimal agreementAmount;
    private String status;

    @OneToMany(mappedBy = "tender")
    private List<Construction> constructions;
}
"@ | Out-File -FilePath "$modelsDir\Tender.java" -Encoding ascii

@"
package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "constructions")
@Data
public class Construction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tender_id")
    @JsonIgnore
    private Tender tender;
    
    private String type;
    private String name;
    private String village;
    private String taluka;
    private String district;
    private BigDecimal physicalProgress;
    private BigDecimal financialProgress;
    private String status;
}
"@ | Out-File -FilePath "$modelsDir\Construction.java" -Encoding ascii

@"
package com.krs.backend.repositories;
import com.krs.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<String> findByUsername(String username);
    User findUserByUsername(String username);
}
"@ | Out-File -FilePath "$repoDir\UserRepository.java" -Encoding ascii

@"
package com.krs.backend.repositories;
import com.krs.backend.models.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenderRepository extends JpaRepository<Tender, Long> {}
"@ | Out-File -FilePath "$repoDir\TenderRepository.java" -Encoding ascii

@"
package com.krs.backend.repositories;
import com.krs.backend.models.Construction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConstructionRepository extends JpaRepository<Construction, Long> {}
"@ | Out-File -FilePath "$repoDir\ConstructionRepository.java" -Encoding ascii

@"
package com.krs.backend.security;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("`${jwt.secret}")
    private String jwtSecret;
    @Value("`${jwt.expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (Exception e) { }
        return false;
    }
}
"@ | Out-File -FilePath "$secDir\JwtUtils.java" -Encoding ascii

@"
package com.krs.backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {}
        filterChain.doFilter(request, response);
    }
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) return headerAuth.substring(7);
        return null;
    }
}
"@ | Out-File -FilePath "$secDir\AuthTokenFilter.java" -Encoding ascii

@"
package com.krs.backend.security;
import com.krs.backend.models.User;
import com.krs.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if(user == null) throw new UsernameNotFoundException("User Not Found");
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).collect(Collectors.toList())
        );
    }
}
"@ | Out-File -FilePath "$secDir\UserDetailsServiceImpl.java" -Encoding ascii

@"
package com.krs.backend.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired AuthTokenFilter authTokenFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
"@ | Out-File -FilePath "$secDir\WebSecurityConfig.java" -Encoding ascii

@"
package com.krs.backend.controllers;
import com.krs.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired AuthenticationManager authenticationManager;
    @Autowired JwtUtils jwtUtils;

    @PostMapping("/login")
    public Map<String, Object> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", authentication.getName());
        return response;
    }
}
"@ | Out-File -FilePath "$ctrlDir\AuthController.java" -Encoding ascii

@"
package com.krs.backend.controllers;
import com.krs.backend.models.Tender;
import com.krs.backend.repositories.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tenders")
public class TenderController {
    @Autowired TenderRepository tenderRepository;
    
    @GetMapping
    public List<Tender> getAllTenders() { return tenderRepository.findAll(); }
    
    @PostMapping
    public Tender createTender(@RequestBody Tender tender) { return tenderRepository.save(tender); }
}
"@ | Out-File -FilePath "$ctrlDir\TenderController.java" -Encoding ascii
