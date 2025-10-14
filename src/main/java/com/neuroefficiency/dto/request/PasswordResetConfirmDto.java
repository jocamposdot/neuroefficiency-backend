package com.neuroefficiency.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para confirmação de reset de senha
 * 
 * Endpoint: POST /api/auth/password-reset/confirm
 * 
 * Request body exemplo:
 * {
 *   "token": "abc123...",
 *   "newPassword": "NewPassword123!",
 *   "confirmPassword": "NewPassword123!"
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
public class PasswordResetConfirmDto {

    /**
     * Token recebido por email (64 caracteres hexadecimais)
     */
    @NotBlank(message = "Token é obrigatório")
    @Size(min = 64, max = 64, message = "Token deve ter 64 caracteres")
    @Pattern(
        regexp = "^[a-f0-9]{64}$",
        message = "Token deve conter apenas caracteres hexadecimais (a-f, 0-9)"
    )
    private String token;

    /**
     * Nova senha do usuário
     * 
     * Requisitos mínimos de segurança:
     * - 8 a 100 caracteres
     * - Pelo menos uma letra maiúscula
     * - Pelo menos uma letra minúscula
     * - Pelo menos um número
     * - Pelo menos um caractere especial
     */
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
        message = "Senha deve conter pelo menos: 1 maiúscula, 1 minúscula, 1 número e 1 caractere especial (@$!%*?&#)"
    )
    private String newPassword;

    /**
     * Confirmação da nova senha (deve ser igual a newPassword)
     * 
     * A validação de igualdade é feita no service layer.
     */
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;
}

