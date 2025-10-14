package com.neuroefficiency.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitação de reset de senha
 * 
 * Endpoint: POST /api/auth/password-reset/request
 * 
 * Request body exemplo:
 * {
 *   "email": "user@example.com"
 * }
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    /**
     * Email do usuário que deseja resetar a senha
     * 
     * Validações:
     * - Não pode ser vazio/null
     * - Deve ser formato de email válido
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
}

