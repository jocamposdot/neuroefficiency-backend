package com.neuroefficiency.dto.request;

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
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
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

