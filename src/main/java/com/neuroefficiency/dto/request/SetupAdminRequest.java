package com.neuroefficiency.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para setup inicial de usuário administrador do sistema.
 * Deve ser usado apenas na primeira execução do sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupAdminRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username deve conter apenas letras, números e underscore")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 8, max = 100, message = "Password deve ter entre 8 e 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
        message = "Password deve conter pelo menos: 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial (@$!%*?&#)"
    )
    private String password;

    @NotBlank(message = "Confirmação de password é obrigatória")
    private String confirmPassword;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
}

