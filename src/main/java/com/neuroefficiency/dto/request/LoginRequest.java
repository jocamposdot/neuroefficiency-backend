package com.neuroefficiency.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login
 * 
 * @author Joao Fuhrmann
 * @version 1.0
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;
}

