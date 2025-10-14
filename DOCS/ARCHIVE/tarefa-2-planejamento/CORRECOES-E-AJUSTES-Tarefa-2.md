# 🔧 CORREÇÕES & AJUSTES - Tarefa 2
## Recuperação de Senha por E-mail

**Data:** 14 de Outubro de 2025  
**Versão:** 1.0  
**Status:** ✅ **RESOLVIDO - Pronto para Implementação**  
**Baseado em:** Revisão & Análise Completa da Tarefa 2

---

## 📋 Sumário Executivo

Este documento resolve **10 problemas críticos** identificados na análise da Especificação Técnica da Tarefa 2, garantindo que a implementação seja:

- ✅ **Funcional**: Sem bugs lógicos
- ✅ **Segura**: Proteções corretas implementadas
- ✅ **Completa**: Sem gaps de configuração
- ✅ **Testável**: Todos os componentes bem definidos

---

## 🔴 PROBLEMAS CRÍTICOS RESOLVIDOS

### 1️⃣ **Token Hash Comparison - PROBLEMA CRÍTICO RESOLVIDO** ✅

**Problema Original:**
```java
// ❌ ISSO NÃO FUNCIONA!
PasswordResetToken resetToken = tokenRepository.findByTokenHash(
    passwordEncoder.encode(token)  // BCrypt gera hash diferente sempre!
).orElseThrow();
```

**Por que não funciona:**
- BCrypt usa **salt aleatório**
- `encode("abc123")` → `$2a$12$XYZ...` (tentativa 1)
- `encode("abc123")` → `$2a$12$DEF...` (tentativa 2) ← **DIFERENTE!**
- Resultado: **NUNCA encontra o token no banco!**

---

**SOLUÇÃO IMPLEMENTADA: SHA-256 para Tokens**

#### **Por que SHA-256 e não BCrypt?**

| Aspecto | BCrypt | SHA-256 |
|---------|--------|---------|
| **Salt aleatório** | ✅ Sim (bom para senhas) | ❌ Não (bom para tokens) |
| **Lookup direto** | ❌ Impossível | ✅ Possível |
| **Segurança** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ (suficiente para tokens) |
| **Performance** | Lento (proposital) | Rápido |
| **Uso correto** | Senhas de usuário | Tokens de sessão, reset |

**Conclusão:** SHA-256 é **perfeito** para tokens de reset porque:
- ✅ Permite lookup direto no banco
- ✅ É unidirecional (seguro)
- ✅ É rápido
- ✅ É determinístico (mesmo input = mesmo hash)

---

#### **Implementação Correta:**

**1. Adicionar dependência (pom.xml):**
```xml
<!-- Já vem com Spring Boot, mas garantir -->
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

**2. Criar Utility Class:**
```java
package com.neuroefficiency.util;

import org.apache.commons.codec.digest.DigestUtils;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Utilitários para geração e hash de tokens
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@UtilityClass
public class TokenUtils {

    /**
     * Gera token seguro de 64 caracteres hexadecimais (256 bits de entropia)
     */
    public static String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Gera hash SHA-256 do token
     * Usa SHA-256 porque é determinístico (mesmo input = mesmo hash)
     * permitindo lookup direto no banco, diferente de BCrypt que usa salt aleatório.
     */
    public static String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }

    /**
     * Verifica se token corresponde ao hash
     */
    public static boolean matches(String token, String hash) {
        return hashToken(token).equals(hash);
    }
}
```

**3. Atualizar PasswordResetService:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    // ... outros repositories

    /**
     * Solicita reset de senha
     */
    @Transactional
    public void requestPasswordReset(String email, String ipAddress, String userAgent, Locale locale) {
        // ... validações

        // ✅ CORRETO: Gerar e hashear token
        String token = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(token);  // SHA-256

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .tokenHash(tokenHash)
            .usuario(usuario)
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();

        tokenRepository.save(resetToken);

        // Enviar email com token em plain text (só vai por email)
        emailService.sendPasswordResetEmail(usuario, token, locale);
    }

    /**
     * Confirma reset com token
     */
    @Transactional
    public void confirmPasswordReset(String token, String newPassword, String ipAddress, String userAgent, Locale locale) {
        
        // ✅ CORRETO: Hashear token recebido e buscar
        String tokenHash = TokenUtils.hashToken(token);
        
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidTokenException("Token inválido ou não encontrado"));

        // Validações
        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Token expirado");
        }

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token já utilizado");
        }

        Usuario usuario = resetToken.getUsuario();

        // Atualizar senha (BCrypt AQUI sim!)
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // Marcar token como usado
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // ... resto do fluxo
    }

    /**
     * Valida token
     */
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        try {
            // ✅ CORRETO: Hashear e buscar
            String tokenHash = TokenUtils.hashToken(token);
            
            PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElse(null);

            if (resetToken == null) {
                return TokenValidationResponse.invalid();
            }

            return TokenValidationResponse.builder()
                .valid(!resetToken.isExpired() && !resetToken.isUsed())
                .expired(resetToken.isExpired())
                .used(resetToken.isUsed())
                .build();

        } catch (Exception e) {
            log.error("Erro ao validar token", e);
            return TokenValidationResponse.invalid();
        }
    }
}
```

**4. Comentário na Migration:**
```sql
-- V3__create_password_reset_tokens.sql
CREATE TABLE password_reset_tokens (
    -- ...
    token_hash VARCHAR(64) NOT NULL UNIQUE,  -- SHA-256 = 64 chars hex
    -- ...
);

COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Hash SHA-256 do token (não BCrypt, precisa lookup direto)';
```

---

### 2️⃣ **Port Mismatch - RESOLVIDO** ✅

**Problema:**
- `application.properties`: porta **8081**
- Frontend configurado: porta **8082**
- Documentação menciona: porta **8082**

**Solução:**
```properties
# src/main/resources/application.properties
server.port=8082  # ⬅️ ATUALIZADO de 8081 para 8082
```

**Justificativa:**
Frontend já está configurado para 8082 em múltiplos lugares. Mais fácil mudar backend.

---

### 3️⃣ **Response Format Strategy - RESOLVIDO** ✅

**Problema:**
- Backend atual retorna: `{ message, user }`
- Frontend espera: `{ success, data, message }`
- Doc propunha wrapper mas não dizia QUANDO aplicar

**DECISÃO: Estratégia Gradual**

#### **Fase 2 (Agora):**
1. ✅ Criar `ApiResponse<T>` wrapper
2. ✅ Aplicar **APENAS nos novos endpoints** de reset
3. ✅ Manter endpoints existentes como estão

#### **Fase 3 (Futuro):**
4. ⏳ Refatorar endpoints existentes gradualmente
5. ⏳ Atualizar frontend para consumir novo formato
6. ⏳ Depreciar formato antigo

**Implementação:**

**1. Criar ApiResponse Wrapper:**
```java
package com.neuroefficiency.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper padronizado para todas as respostas da API
 * 
 * @param <T> Tipo do dado retornado
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indica se a operação foi bem-sucedida
     */
    private Boolean success;

    /**
     * Dados retornados (pode ser null)
     */
    private T data;

    /**
     * Mensagem descritiva (opcional)
     */
    private String message;

    /**
     * Timestamp da resposta (opcional)
     */
    private Long timestamp;

    /**
     * Cria resposta de sucesso com dados e mensagem
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * Cria resposta de sucesso apenas com dados
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    /**
     * Cria resposta de sucesso apenas com mensagem
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * Cria resposta de erro com mensagem
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * Cria resposta de erro com dados e mensagem
     */
    public static <T> ApiResponse<T> error(T data, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .data(data)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

**2. Usar APENAS em endpoints novos:**
```java
// PasswordResetController (NOVO - usa ApiResponse)
@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestReset(...) {
        passwordResetService.requestPasswordReset(...);
        return ResponseEntity.ok(
            ApiResponse.success("Se o email existir, você receberá instruções")
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReset(...) {
        passwordResetService.confirmPasswordReset(...);
        return ResponseEntity.ok(
            ApiResponse.success("Senha redefinida com sucesso")
        );
    }
}

// AuthController (EXISTENTE - mantém formato antigo)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(...) {  // ⬅️ Formato antigo mantido
        // ... mantém como está
    }
}
```

**3. Documentar no CHANGELOG:**
```markdown
## [Fase 2] - 2025-10-14

### Adicionado
- Novo wrapper `ApiResponse<T>` para padronização futura
- Endpoints de reset usam novo formato

### Mantido (sem quebras)
- Endpoints existentes mantêm formato antigo
- Frontend continua funcionando 100%

### Planejado (Fase 3)
- Migração gradual de todos os endpoints
```

---

### 4️⃣ **RegisterRequest SEM Campo Email - RESOLVIDO** ✅

**Problema:**
Como usuário vai registrar com email se o endpoint não aceita?

**Solução:**
```java
package com.neuroefficiency.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de registro de novo usuário
 * 
 * @author Neuroefficiency Team
 * @version 2.0 - Adicionado campo email
 * @since 2025-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Username deve conter apenas letras, números, _ ou -"
    )
    private String username;

    // ✅ NOVO: Campo email obrigatório
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 8, max = 100, message = "Password deve ter entre 8 e 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "Confirmação de password é obrigatória")
    private String confirmPassword;
}
```

**Atualizar AuthenticationService:**
```java
@Service
public class AuthenticationService {

    public Usuario register(RegisterRequest request) {
        // Validar email único
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email já cadastrado");
        }

        // Validar username único
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username já existe");
        }

        // Validar senhas coincidem
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Senhas não coincidem");
        }

        // Criar usuário
        Usuario usuario = Usuario.builder()
            .username(request.getUsername())
            .email(request.getEmail())  // ⬅️ NOVO
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        return usuarioRepository.save(usuario);
    }
}
```

**Criar Exception:**
```java
package com.neuroefficiency.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
```

**Adicionar ao GlobalExceptionHandler:**
```java
@ExceptionHandler(EmailAlreadyExistsException.class)
public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
        EmailAlreadyExistsException ex) {
    
    Map<String, Object> error = buildErrorResponse(
        HttpStatus.CONFLICT,
        "Email já existe",
        ex.getMessage()
    );
    
    log.warn("Tentativa de registro com email duplicado");
    
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}
```

---

### 5️⃣ **UserResponse SEM Campo Email - RESOLVIDO** ✅

**Problema:**
Backend vai ter email no Usuario mas não retorna no UserResponse.

**Solução:**
```java
package com.neuroefficiency.dto.response;

import com.neuroefficiency.domain.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta contendo dados do usuário
 * 
 * @author Neuroefficiency Team
 * @version 2.0 - Adicionado campo email
 * @since 2025-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;  // ✅ NOVO
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converte uma entidade Usuario para UserResponse
     */
    public static UserResponse from(Usuario usuario) {
        return UserResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())  // ✅ NOVO
                .enabled(usuario.getEnabled())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}
```

---

### 6️⃣ **SecurityConfig - Endpoints Públicos - RESOLVIDO** ✅

**Problema:**
Novos endpoints de reset precisam ser públicos mas doc não especificou.

**Solução:**
```java
package com.neuroefficiency.config;

import com.neuroefficiency.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Configuração de Segurança do Spring Security
 * 
 * @author Neuroefficiency Team
 * @version 2.0 - Adicionados endpoints de reset de senha
 * @since 2025-10-14
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Configuração do PasswordEncoder com BCrypt
     * Força 12 (recomendado para ambientes de saúde)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuração da cadeia de filtros de segurança
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/health",
                    "/api/auth/password-reset/request",       // ✅ NOVO
                    "/api/auth/password-reset/confirm",       // ✅ NOVO
                    "/api/auth/password-reset/validate-token/**"  // ✅ NOVO
                ).permitAll()
                
                // Endpoints autenticados
                .requestMatchers(
                    "/api/auth/me",
                    "/api/auth/logout"
                ).authenticated()
                
                // Qualquer outra requisição requer autenticação
                .anyRequest().authenticated()
            )
            
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            )
            
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        
        return authBuilder.build();
    }
}
```

---

### 7️⃣ **Thymeleaf Dependency - RESOLVIDO** ✅

**Problema:**
Doc usa templates Thymeleaf mas dependência não estava especificada.

**Solução:**
```xml
<!-- pom.xml -->

<!-- Email com Templates Thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Spring Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

### 8️⃣ **MessageSource Configuration - RESOLVIDO** ✅

**Problema:**
Doc usa i18n mas não especificou configuração Spring.

**Solução:**
```java
package com.neuroefficiency.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configuração de Internacionalização (i18n)
 * 
 * Suporta múltiplos idiomas baseado em header Accept-Language.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Configuration
public class I18nConfig {

    /**
     * Configuração do MessageSource para i18n
     * 
     * Arquivos esperados:
     * - src/main/resources/messages.properties (fallback)
     * - src/main/resources/messages_pt_BR.properties
     * - src/main/resources/messages_en_US.properties
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Resolver de locale baseado em Accept-Language header
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        resolver.setSupportedLocales(java.util.List.of(
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("en-US")
        ));
        return resolver;
    }
}
```

---

### 9️⃣ **@EnableScheduling - RESOLVIDO** ✅

**Problema:**
Job de cleanup usa `@Scheduled` mas `@EnableScheduling` não estava ativado.

**Solução:**
```java
package com.neuroefficiency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicação principal do Neuroefficiency
 * 
 * @author Neuroefficiency Team
 * @version 2.0 - Adicionado @EnableScheduling para jobs de limpeza
 * @since 2025-10-14
 */
@SpringBootApplication
@EnableScheduling  // ✅ NOVO: Habilita jobs agendados
public class NeuroefficiencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuroefficiencyApplication.class, args);
    }
}
```

---

### 🔟 **validateToken() - Mesmo Problema de Hash - RESOLVIDO** ✅

**Problema:**
Método `validateToken()` tinha o mesmo problema de hash do BCrypt.

**Solução:**
Já foi resolvido na correção #1 (usar `TokenUtils.hashToken()` com SHA-256).

---

## ✅ DEPENDÊNCIAS COMPLETAS (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <!-- ... -->
    
    <dependencies>
        <!-- Dependências Existentes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- ✅ NOVAS DEPENDÊNCIAS - Tarefa 2 -->
        
        <!-- Email -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        
        <!-- Templates Thymeleaf para emails -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- Apache Commons Codec (SHA-256 para tokens) -->
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
        </dependency>
        
        <!-- Testes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- ... -->
</project>
```

---

## 📝 CUSTOM EXCEPTIONS DEFINIDAS

```java
// InvalidTokenException.java
package com.neuroefficiency.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

// TokenExpiredException.java
package com.neuroefficiency.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}

// RateLimitExceededException.java
package com.neuroefficiency.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}

// EmailSendingException.java
package com.neuroefficiency.exception;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

// EmailAlreadyExistsException.java
package com.neuroefficiency.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
```

**Atualizar GlobalExceptionHandler:**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ... handlers existentes

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpired(TokenExpiredException ex) {
        log.warn("Token expirado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("Rate limit excedido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailSending(EmailSendingException ex) {
        log.error("Erro ao enviar email", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Erro ao enviar email. Tente novamente mais tarde."));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email já existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

---

## 🛠️ MAILHOG INSTALLATION GUIDE

### Opção 1: Docker (RECOMENDADO) 🐳

```bash
# Rodar MailHog via Docker
docker run -d \
  --name mailhog \
  -p 1025:1025 \
  -p 8025:8025 \
  mailhog/mailhog

# Verificar se está rodando
docker ps | grep mailhog

# Acessar UI web
open http://localhost:8025
```

### Opção 2: Download Executável

**Windows:**
```powershell
# Download
Invoke-WebRequest -Uri "https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_windows_amd64.exe" -OutFile "MailHog.exe"

# Executar
./MailHog.exe
```

**Linux/Mac:**
```bash
# Download
wget https://github.com/mailhog/MailHog/releases/download/v1.0.1/MailHog_linux_amd64
chmod +x MailHog_linux_amd64

# Executar
./MailHog_linux_amd64
```

### Configuração no Backend

```properties
# application-dev.properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false

# URL do MailHog UI
mailhog.ui.url=http://localhost:8025
```

---

## 🌍 ENVIRONMENT VARIABLES CONSOLIDADAS

### Desenvolvimento (.env.dev)
```properties
# Server
SERVER_PORT=8082

# Database
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/neuroefficiency
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# Email
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_FROM=noreply@neuroefficiency.local

# Frontend
FRONTEND_URL=http://localhost:5173

# Profile
SPRING_PROFILES_ACTIVE=dev
```

### Produção (.env.prod)
```properties
# Server
SERVER_PORT=8082

# Database
SPRING_DATASOURCE_URL=${DATABASE_URL}
SPRING_DATASOURCE_USERNAME=${DATABASE_USERNAME}
SPRING_DATASOURCE_PASSWORD=${DATABASE_PASSWORD}

# Email (AWS SES, SendGrid, etc.)
SMTP_HOST=${SMTP_HOST}
SMTP_PORT=${SMTP_PORT}
SMTP_USERNAME=${SMTP_USERNAME}
SMTP_PASSWORD=${SMTP_PASSWORD}
SMTP_FROM=noreply@neuroefficiency.com

# Frontend
FRONTEND_URL=https://app.neuroefficiency.com

# Profile
SPRING_PROFILES_ACTIVE=prod
```

---

## 📊 AJUSTES ADICIONAIS

### 1. Job de Limpeza Melhorado

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final PasswordResetTokenRepository tokenRepository;

    /**
     * Limpa tokens expirados e usados diariamente às 3h
     */
    @Scheduled(cron = "0 0 3 * * ?", zone = "America/Sao_Paulo")
    @Transactional
    public void cleanupTokens() {
        log.info("Iniciando limpeza de tokens");

        try {
            int deleted = tokenRepository.deleteExpiredOrUsed(LocalDateTime.now());
            log.info("Limpeza concluída: {} tokens removidos", deleted);
        } catch (Exception e) {
            log.error("Erro ao limpar tokens", e);
        }
    }
}
```

**Repository:**
```java
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now OR t.usedAt IS NOT NULL")
    int deleteExpiredOrUsed(@Param("now") LocalDateTime now);
    
    // ... outros métodos
}
```

---

### 2. sanitizeEmail() Seguro

```java
private String sanitizeEmail(String email) {
    if (email == null || email.isBlank()) {
        return "***";
    }
    
    if (!email.contains("@")) {
        return "***";
    }
    
    String[] parts = email.split("@");
    if (parts.length != 2 || parts[0].isEmpty()) {
        return "***@***";
    }
    
    return parts[0].charAt(0) + "***@" + parts[1];
}
```

---

### 3. Accept-Language Parser Seguro

```java
@PostMapping("/request")
public ResponseEntity<ApiResponse<Void>> requestReset(
        @Valid @RequestBody PasswordResetRequestDto request,
        @RequestHeader(value = "Accept-Language", required = false) String languageHeader,
        HttpServletRequest httpRequest) {

    // Parse seguro do locale
    String lang = Optional.ofNullable(languageHeader)
        .map(h -> h.split(",")[0].trim())
        .orElse("pt-BR");
    Locale locale = Locale.forLanguageTag(lang);

    // ... resto
}
```

---

### 4. Migration V2 com Email Opcional (Usuários Legacy)

```sql
-- V2__add_email_to_usuarios.sql

-- Adicionar coluna email (NULLABLE temporariamente)
ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);

-- Criar índice parcial único (só para emails não-nulos)
CREATE UNIQUE INDEX uk_usuarios_email ON usuarios(email) WHERE email IS NOT NULL;

-- Criar índice para performance
CREATE INDEX idx_usuarios_email ON usuarios(email);

-- Comentário
COMMENT ON COLUMN usuarios.email IS 'Email do usuário (obrigatório para novos registros, opcional para usuários legacy da Fase 1)';
```

**Atualizar Usuario.java:**
```java
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    
    // ... outros campos
    
    @Email(message = "Email deve ser válido")
    @Column(unique = true, length = 255)  // Nullable = true (padrão)
    private String email;  // Opcional para usuários legacy
    
    // ... resto
}
```

**Validação no AuthenticationService:**
```java
public Usuario register(RegisterRequest request) {
    // Email é obrigatório no registro
    if (request.getEmail() == null || request.getEmail().isBlank()) {
        throw new IllegalArgumentException("Email é obrigatório");
    }
    
    // ... resto
}
```

---

## ✅ CHECKLIST DE IMPLEMENTAÇÃO ATUALIZADO

### Etapa 1: Configuração de Infraestrutura
- [ ] Adicionar dependências ao pom.xml (mail, thymeleaf, commons-codec)
- [ ] Atualizar `server.port=8082` no application.properties
- [ ] Criar `I18nConfig.java`
- [ ] Adicionar `@EnableScheduling` no Application
- [ ] Instalar e configurar MailHog (Docker)
- [ ] Configurar properties de email (dev/test/prod)
- [ ] Testar envio de email básico

### Etapa 2: Estrutura de Dados
- [ ] Criar `TokenUtils.java` (SHA-256)
- [ ] Criar migration V2 (adicionar email - nullable)
- [ ] Criar migration V3 (tabela tokens - VARCHAR(64))
- [ ] Criar migration V4 (tabela audit)
- [ ] Atualizar entidade Usuario (email nullable)
- [ ] Criar entidade PasswordResetToken
- [ ] Criar entidade PasswordResetAudit
- [ ] Criar enums (AuditEventType)
- [ ] Criar repositories (com deleteExpiredOrUsed)
- [ ] Testar migrations

### Etapa 3: DTOs e Responses
- [ ] Criar `ApiResponse<T>` wrapper
- [ ] Atualizar `RegisterRequest` (adicionar email)
- [ ] Atualizar `UserResponse` (adicionar email)
- [ ] Criar `PasswordResetRequestDto`
- [ ] Criar `PasswordResetConfirmDto`
- [ ] Criar `TokenValidationResponse`
- [ ] Criar Custom Exceptions (5 novas)
- [ ] Atualizar `GlobalExceptionHandler`

### Etapa 4: Configuração
- [ ] Atualizar `SecurityConfig` (endpoints públicos)
- [ ] Verificar CORS configuration

### Etapa 5: Services
- [ ] Implementar `EmailService`
- [ ] Criar templates Thymeleaf (password-reset.html, password-changed.html)
- [ ] Criar arquivos i18n (messages_pt_BR, messages_en_US)
- [ ] Implementar `PasswordResetService` (usando TokenUtils)
- [ ] Criar `TokenCleanupJob`
- [ ] Testar envio de emails

### Etapa 6: Controllers
- [ ] Criar `PasswordResetController`
- [ ] Implementar 3 endpoints (request, confirm, validate)
- [ ] Testar via Postman

### Etapa 7: Testes
- [ ] Testes unitários PasswordResetService (12+)
- [ ] Testes unitários EmailService (4+)
- [ ] Testes de integração Controller (8+)
- [ ] Testes de rate limiting
- [ ] Testes de token validation
- [ ] Garantir > 80% cobertura

### Etapa 8: Documentação
- [ ] Atualizar Postman Collection
- [ ] Atualizar README.md
- [ ] Criar guia MailHog
- [ ] Atualizar CHANGELOG

### Etapa 9: Revisão Final
- [ ] Code review
- [ ] Verificar linters
- [ ] Todos os testes passando
- [ ] Validar critérios de aceitação

---

## 🎯 RESUMO DAS MUDANÇAS

| # | Problema | Solução | Status |
|---|----------|---------|--------|
| 1 | Token hash BCrypt | SHA-256 com TokenUtils | ✅ Resolvido |
| 2 | Port mismatch | server.port=8082 | ✅ Resolvido |
| 3 | Response format | ApiResponse só em novos endpoints | ✅ Resolvido |
| 4 | RegisterRequest sem email | Adicionado campo email | ✅ Resolvido |
| 5 | UserResponse sem email | Adicionado campo email | ✅ Resolvido |
| 6 | SecurityConfig | Endpoints públicos definidos | ✅ Resolvido |
| 7 | Thymeleaf dependency | Adicionada ao pom.xml | ✅ Resolvido |
| 8 | MessageSource config | I18nConfig criado | ✅ Resolvido |
| 9 | @EnableScheduling | Adicionado ao Application | ✅ Resolvido |
| 10 | validateToken() | Usa TokenUtils | ✅ Resolvido |

---

## ✅ CONCLUSÃO

**Todos os 10 problemas críticos foram resolvidos!** 🎉

A especificação está agora:
- ✅ **Funcional**: Lógica de token hash corrigida
- ✅ **Completa**: Configurações, dependências e exceptions definidas
- ✅ **Segura**: SHA-256 para tokens, BCrypt para senhas
- ✅ **Pronta**: Pode-se iniciar implementação com confiança

---

## 🚀 PRÓXIMOS PASSOS

1. ✅ **Atualizar Especificação Técnica** com essas correções
2. ✅ **Commitar documento de correções**
3. ✅ **Iniciar implementação** seguindo a ordem do checklist

---

**Preparado por:** AI Assistant  
**Data:** 14 de Outubro de 2025  
**Status:** ✅ **APROVADO PARA IMPLEMENTAÇÃO**  
**Próximo Passo:** Atualizar especificação técnica e começar codificação

